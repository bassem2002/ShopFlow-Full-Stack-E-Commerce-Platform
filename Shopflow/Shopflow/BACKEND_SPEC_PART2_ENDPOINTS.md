# ShopFlow — Backend Spec · Partie 2 : API Endpoints (Controllers)

> **Base URL** : `http://localhost:8080`  
> **Format** : JSON (`Content-Type: application/json`)  
> **Auth** : `Authorization: Bearer <accessToken>` (sauf routes publiques)

---

## Conventions de réponse

| Situation            | HTTP Status          | Angular note                      |
|----------------------|----------------------|-----------------------------------|
| Création réussie     | `201 Created`        | `response.status === 201`         |
| Lecture / Update OK  | `200 OK`             |                                   |
| Suppression OK       | `204 No Content`     | pas de body                       |
| Non authentifié      | `401 Unauthorized`   | → rediriger vers `/login`         |
| Accès refusé         | `403 Forbidden`      | → afficher page d'erreur          |
| Ressource introuvable| `404 Not Found`      |                                   |
| Erreur de validation | `400 Bad Request`    | lire `message` dans le body       |

### Structure d'une Page (liste paginée)
```typescript
// Ce que Spring retourne pour Page<T>
export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;       // page courante (0-indexed)
  first: boolean;
  last: boolean;
}
```

---

## 1. AuthController — `/api/auth`

> 🔓 **Tous les endpoints sont publics** (aucun token requis)

---

### POST `/api/auth/register`
Créer un compte (CUSTOMER ou SELLER).

**Request Body :**
```json
{
  "email": "john@example.com",
  "password": "secret123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "CUSTOMER"
}
```
> Pour un `SELLER`, ajouter : `"storeName"`, `"storeDescription"`, `"storeLogo"`

**Response `201` :**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "uuid-refresh-token",
  "email": "john@example.com",
  "role": "CUSTOMER"
}
```

**Angular :**
```typescript
this.http.post<AuthResponse>('/api/auth/register', body)
```

---

### POST `/api/auth/login`
Connexion — retourne les tokens JWT.

**Request Body :**
```json
{
  "email": "john@example.com",
  "password": "secret123"
}
```

**Response `200` :**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "uuid-refresh-token",
  "email": "john@example.com",
  "role": "CUSTOMER"
}
```

**Angular :**
```typescript
this.http.post<AuthResponse>('/api/auth/login', body)
```

---

### POST `/api/auth/refresh`
Renouveler l'`accessToken` avec le `refreshToken`.

**Request Body :**
```json
{ "refreshToken": "uuid-refresh-token" }
```

**Response `200` :** même structure qu'`AuthResponse`

---

### POST `/api/auth/logout`
Invalider le refresh token en base.

**Request Body :**
```json
{ "refreshToken": "uuid-refresh-token" }
```

**Response `204` :** aucun body

---

## 2. ProductController — `/api/products`

| Méthode | URL | Sécurité |
|---------|-----|----------|
| GET | `/api/products` | 🔓 Public |
| GET | `/api/products/{id}` | 🔓 Public |
| GET | `/api/products/search?q=` | 🔓 Public |
| GET | `/api/products/top-selling` | 🔓 Public |
| POST | `/api/products` | 🔐 SELLER / ADMIN |
| PUT | `/api/products/{id}` | 🔐 SELLER / ADMIN |
| DELETE | `/api/products/{id}` | 🔐 SELLER / ADMIN |

---

### GET `/api/products`
Liste paginée avec filtres.

**Query Params :**

| Param | Type | Défaut | Description |
|-------|------|--------|-------------|
| `categoryId` | `Long` | — | Filtre par catégorie |
| `minPrice` | `Double` | — | Prix minimum |
| `maxPrice` | `Double` | — | Prix maximum |
| `sellerId` | `Long` | — | Filtre par vendeur |
| `promo` | `Boolean` | — | `true` = uniquement en promo |
| `page` | `int` | `0` | Numéro de page |
| `size` | `int` | `10` | Taille de page |
| `sortBy` | `String` | `createdAt` | Champ de tri |
| `sortDirection` | `String` | `desc` | `asc` ou `desc` |

**Response `200` :** `SpringPage<ProductResponse>`

**Angular :**
```typescript
getProducts(filters: ProductFilter): Observable<SpringPage<ProductResponse>> {
  let params = new HttpParams();
  if (filters.categoryId) params = params.set('categoryId', filters.categoryId);
  if (filters.page !== undefined) params = params.set('page', filters.page);
  return this.http.get<SpringPage<ProductResponse>>('/api/products', { params });
}
```

---

### GET `/api/products/{id}`
Détail d'un produit.

**Path Param :** `id` (Long)

**Response `200` :** `ProductResponse`
```json
{
  "id": 1,
  "sellerId": 2,
  "sellerEmail": "seller@shop.com",
  "name": "T-shirt Classic",
  "description": "100% coton",
  "price": 29.99,
  "promoPrice": 19.99,
  "stock": 100,
  "active": true,
  "createdAt": "2026-04-22T10:00:00",
  "categoryIds": [1, 3],
  "images": ["https://cdn.../img1.jpg"],
  "variants": [
    { "id": 1, "attribute": "Size", "value": "M", "stockAdditional": 50, "priceDelta": 0.0 }
  ],
  "averageRating": 4.5
}
```

---

### GET `/api/products/search?q={terme}`
Recherche textuelle (nom / description).

**Query Params :** `q` (String), `page` (int, défaut 0), `size` (int, défaut 10)

**Response `200` :** `SpringPage<ProductResponse>`

---

### GET `/api/products/top-selling`
Top produits les plus vendus.

**Response `200` :** `ProductResponse[]`

---

### POST `/api/products`
Créer un produit. 🔐 `SELLER` ou `ADMIN`

**Request Body :**
```json
{
  "sellerId": 2,
  "name": "T-shirt Classic",
  "description": "100% coton bio",
  "price": 29.99,
  "promoPrice": null,
  "stock": 100,
  "categoryIds": [1, 3],
  "images": ["https://cdn.../img1.jpg"],
  "variants": [
    { "attribute": "Size", "value": "M", "stockAdditional": 50, "priceDelta": 0.0 },
    { "attribute": "Size", "value": "XL", "stockAdditional": 20, "priceDelta": 5.0 }
  ]
}
```

**Response `201` :** `ProductResponse`

---

### PUT `/api/products/{id}`
Modifier un produit. 🔐 `SELLER` ou `ADMIN`

**Path Param :** `id` (Long)  
**Request Body :** même structure que `ProductRequest`  
**Response `200` :** `ProductResponse`

---

### DELETE `/api/products/{id}`
**Soft-delete** (met `active = false`). 🔐 `SELLER` ou `ADMIN`

**Path Param :** `id` (Long)  
**Response `204` :** aucun body

---

## 3. CategoryController — `/api/categories`

| Méthode | URL | Sécurité |
|---------|-----|----------|
| GET | `/api/categories` | 🔓 Public |
| GET | `/api/categories/tree` | 🔓 Public |
| GET | `/api/categories/{id}` | 🔓 Public |
| POST | `/api/categories` | 🔐 ADMIN |
| PUT | `/api/categories/{id}` | 🔐 ADMIN |
| DELETE | `/api/categories/{id}` | 🔐 ADMIN |

---

### GET `/api/categories`
Liste plate de toutes les catégories.

**Response `200` :** `CategoryResponse[]`
```json
[
  { "id": 1, "name": "Vêtements", "description": null, "parentId": null, "children": [] },
  { "id": 2, "name": "T-shirts",  "description": null, "parentId": 1,    "children": [] }
]
```

---

### GET `/api/categories/tree`
Arbre hiérarchique des catégories (enfants imbriqués).

**Response `200` :** `CategoryResponse[]` (récursif)
```json
[
  {
    "id": 1, "name": "Vêtements", "parentId": null,
    "children": [
      { "id": 2, "name": "T-shirts", "parentId": 1, "children": [] },
      { "id": 3, "name": "Pantalons","parentId": 1, "children": [] }
    ]
  }
]
```

**Angular :** idéal pour construire un menu de navigation récursif.

---

### GET `/api/categories/{id}`
Détail d'une catégorie.

**Response `200` :** `CategoryResponse`

---

### POST `/api/categories`
Créer une catégorie. 🔐 `ADMIN`

**Request Body :**
```json
{
  "name": "Chaussures",
  "description": "Toutes les chaussures",
  "parentId": null
}
```

**Response `201` :** `CategoryResponse`

---

### PUT `/api/categories/{id}`
Modifier une catégorie. 🔐 `ADMIN`

**Request Body :** même que `CategoryRequest`  
**Response `200` :** `CategoryResponse`

---

### DELETE `/api/categories/{id}`
Supprimer une catégorie. 🔐 `ADMIN`

**Response `204` :** aucun body

---

## 4. OrderController — `/api/orders`

> 🔐 Tous les endpoints nécessitent une authentification (`CUSTOMER`, `SELLER` ou `ADMIN`)

| Méthode | URL | Sécurité | Description |
|---------|-----|----------|-------------|
| POST | `/api/orders` | CUSTOMER | Passer une commande depuis le panier |
| GET | `/api/orders/my` | CUSTOMER | Mes commandes |
| GET | `/api/orders/{id}` | Authentifié | Détail d'une commande |
| PUT | `/api/orders/{id}/cancel` | CUSTOMER | Annuler une commande |
| PUT | `/api/orders/{id}/status` | ADMIN / SELLER | Changer le statut |

---

### POST `/api/orders`
Convertit le panier du client en commande. 🔐 `CUSTOMER`

**Request Body :**
```json
{ "shippingAddress": "12 Rue de Paris, Tunis 1000" }
```

**Response `200` :** `OrderResponse`
```json
{
  "id": 42,
  "orderNumber": "ORD-2026-00042",
  "status": "PENDING",
  "shippingAddress": "12 Rue de Paris, Tunis 1000",
  "subTotal": 59.98,
  "shippingCost": 7.0,
  "total": 66.98,
  "createdAt": "2026-04-22T10:30:00",
  "items": [
    { "productId": 1, "productName": "T-shirt Classic", "variantId": 1, "variant": "Size: M", "quantity": 2, "unitPrice": 29.99 }
  ]
}
```

---

### GET `/api/orders/my`
Historique des commandes du client connecté. 🔐 `CUSTOMER`

**Response `200` :** `OrderResponse[]`

**Angular :**
```typescript
getMyOrders(): Observable<OrderResponse[]> {
  return this.http.get<OrderResponse[]>('/api/orders/my');
  // Le token JWT est envoyé via un intercepteur HTTP
}
```

---

### GET `/api/orders/{id}`
Détail d'une commande spécifique.

**Path Param :** `id` (Long)  
**Response `200` :** `OrderResponse`

---

### PUT `/api/orders/{id}/cancel`
Annuler une commande (seulement si `PENDING`). 🔐 `CUSTOMER`

**Response `200` :** `OrderResponse` avec `status: "CANCELLED"`

---

### PUT `/api/orders/{id}/status`
Modifier le statut d'une commande. 🔐 `ADMIN` / `SELLER`

**Query Param :** `status` (String) — ex: `?status=SHIPPED`

**Valeurs valides :** `PENDING` | `PAID` | `PROCESSING` | `SHIPPED` | `DELIVERED` | `CANCELLED` | `REFUNDED`

**Response `200` :** `OrderResponse`

---

## 5. CartController — `/api/cart`

> 🔐 Tous les endpoints nécessitent le rôle `CUSTOMER`

| Méthode | URL | Description |
|---------|-----|-------------|
| GET | `/api/cart?customerId={id}` | Voir son panier |
| POST | `/api/cart/items` | Ajouter un article |
| PUT | `/api/cart/items/{itemId}` | Modifier la quantité |
| DELETE | `/api/cart/items/{itemId}` | Supprimer un article |
| POST | `/api/cart/coupon` | Appliquer un coupon |
| DELETE | `/api/cart/coupon?customerId={id}` | Retirer le coupon |

---

### GET `/api/cart?customerId={id}`
Récupérer le panier d'un client.

**Response `200` :** `CartResponse`
```json
{
  "cartId": 5,
  "customerId": 10,
  "items": [
    { "itemId": 1, "productId": 1, "productName": "T-shirt", "variantId": null, "variantLabel": null, "quantity": 2, "unitPrice": 29.99, "lineTotal": 59.98 }
  ],
  "couponCode": null,
  "subTotal": 59.98,
  "shippingFees": 7.0,
  "totalTTC": 66.98
}
```

---

### POST `/api/cart/items`
Ajouter un produit au panier.

**Request Body :**
```json
{
  "customerId": 10,
  "productId": 1,
  "variantId": null,
  "quantity": 2
}
```

**Response `200` :** `CartResponse` (panier mis à jour)

---

### PUT `/api/cart/items/{itemId}`
Modifier la quantité d'un article.

**Path Param :** `itemId` (Long)  
**Request Body :**
```json
{ "quantity": 3 }
```

**Response `200` :** `CartResponse`

---

### DELETE `/api/cart/items/{itemId}`
Supprimer un article du panier.

**Response `200` :** `CartResponse` (panier mis à jour)

---

### POST `/api/cart/coupon`
Appliquer un code promo au panier.

**Request Body :**
```json
{ "customerId": 10, "couponCode": "SUMMER10" }
```

**Response `200` :** `CartResponse` avec `couponCode: "SUMMER10"` et totaux recalculés

---

### DELETE `/api/cart/coupon?customerId={id}`
Retirer le coupon appliqué.

**Response `200` :** `CartResponse` avec `couponCode: null`

---

## 6. CouponController — `/api/coupons`

| Méthode | URL | Sécurité |
|---------|-----|----------|
| POST | `/api/coupons` | 🔐 ADMIN |
| PUT | `/api/coupons/{id}` | 🔐 ADMIN |
| DELETE | `/api/coupons/{id}` | 🔐 ADMIN |
| GET | `/api/coupons` | 🔐 ADMIN |
| GET | `/api/coupons/{id}` | 🔐 Authentifié |
| GET | `/api/coupons/code/{code}` | 🔓 Public |
| GET | `/api/coupons/validate/{code}` | 🔓 Public |

---

### POST `/api/coupons`
Créer un coupon. 🔐 `ADMIN`

**Request Body :**
```json
{
  "code": "SUMMER10",
  "type": "PERCENT",
  "value": 10.0,
  "expirationDate": "2026-12-31T23:59:59",
  "maxUsages": 100
}
```

**Response `201` :** `CouponResponse`

---

### GET `/api/coupons/validate/{code}`
Vérifier si un coupon est valide (non expiré, actif, usages restants).

**Response `200` :** `true` ou `false`

**Angular :**
```typescript
validateCoupon(code: string): Observable<boolean> {
  return this.http.get<boolean>(`/api/coupons/validate/${code}`);
}
```

---

### GET `/api/coupons/code/{code}`
Récupérer les infos d'un coupon par son code.

**Response `200` :** `CouponResponse`

---

## 7. ReviewController — `/api/reviews`

| Méthode | URL | Sécurité |
|---------|-----|----------|
| POST | `/api/reviews?userId={id}` | 🔐 CUSTOMER |
| PUT | `/api/reviews/{reviewId}?userId={id}` | 🔐 CUSTOMER |
| DELETE | `/api/reviews/{reviewId}?userId={id}` | 🔐 CUSTOMER |
| GET | `/api/reviews/{id}` | 🔓 Public |
| GET | `/api/reviews/product/{productId}` | 🔓 Public |
| GET | `/api/reviews/user/{userId}` | 🔓 Public |

---

### POST `/api/reviews?userId={id}`
Soumettre un avis. 🔐 `CUSTOMER`

**Request Body :**
```json
{
  "productId": 1,
  "rating": 5,
  "comment": "Excellent produit, livraison rapide !"
}
```

**Response `201` :** `ReviewResponse`

---

### GET `/api/reviews/product/{productId}`
Tous les avis pour un produit.

**Response `200` :** `ReviewResponse[]`
```json
[
  {
    "id": 1, "productId": 1, "productName": "T-shirt Classic",
    "userId": 10, "userName": "john@example.com",
    "rating": 5, "comment": "Parfait !", "createdAt": "2026-04-20T14:00:00"
  }
]
```

---

### PUT `/api/reviews/{reviewId}?userId={id}`
Modifier son avis.

**Request Body :**
```json
{ "productId": 1, "rating": 4, "comment": "Très bien finalement" }
```

**Response `200` :** `ReviewResponse`

---

### DELETE `/api/reviews/{reviewId}?userId={id}`
Supprimer son avis.

**Response `204` :** aucun body

---

## 8. DashboardController — `/api/dashboard`

| Méthode | URL | Sécurité | Description |
|---------|-----|----------|-------------|
| GET | `/api/dashboard/admin` | 🔐 ADMIN | Stats globales |
| GET | `/api/dashboard/seller` | 🔐 SELLER | Stats du vendeur connecté |
| GET | `/api/dashboard/customer` | 🔐 CUSTOMER | Stats du client connecté |

---

### GET `/api/dashboard/admin`
Statistiques globales de la plateforme. 🔐 `ADMIN`

**Response `200` :** `AdminDashboard`
```json
{
  "totalRevenue": 15420.50,
  "totalOrders": 342,
  "totalProducts": 128,
  "totalUsers": 890,
  "recentOrders": [ ... ],
  "topProducts": [ ... ]
}
```

---

### GET `/api/dashboard/seller`
Dashboard du vendeur connecté. 🔐 `SELLER`

**Response `200` :** `SellerDashboard`
```json
{
  "totalRevenue": 3200.00,
  "totalOrders": 45,
  "totalProducts": 12,
  "topProducts": [ ... ]
}
```

---

### GET `/api/dashboard/customer`
Dashboard du client connecté. 🔐 `CUSTOMER`

**Response `200` :** `CustomerDashboard`
```json
{
  "totalOrders": 8,
  "totalSpent": 450.00,
  "recentOrders": [ ... ]
}
```

---

## 9. Récapitulatif Global des Endpoints

| # | Méthode | URL | Auth | Response |
|---|---------|-----|------|----------|
| 1 | POST | `/api/auth/register` | 🔓 | `AuthResponse` (201) |
| 2 | POST | `/api/auth/login` | 🔓 | `AuthResponse` (200) |
| 3 | POST | `/api/auth/refresh` | 🔓 | `AuthResponse` (200) |
| 4 | POST | `/api/auth/logout` | 🔓 | `204` |
| 5 | GET | `/api/products` | 🔓 | `SpringPage<ProductResponse>` |
| 6 | GET | `/api/products/{id}` | 🔓 | `ProductResponse` |
| 7 | GET | `/api/products/search?q=` | 🔓 | `SpringPage<ProductResponse>` |
| 8 | GET | `/api/products/top-selling` | 🔓 | `ProductResponse[]` |
| 9 | POST | `/api/products` | SELLER/ADMIN | `ProductResponse` (201) |
| 10 | PUT | `/api/products/{id}` | SELLER/ADMIN | `ProductResponse` |
| 11 | DELETE | `/api/products/{id}` | SELLER/ADMIN | `204` |
| 12 | GET | `/api/categories` | 🔓 | `CategoryResponse[]` |
| 13 | GET | `/api/categories/tree` | 🔓 | `CategoryResponse[]` |
| 14 | GET | `/api/categories/{id}` | 🔓 | `CategoryResponse` |
| 15 | POST | `/api/categories` | ADMIN | `CategoryResponse` (201) |
| 16 | PUT | `/api/categories/{id}` | ADMIN | `CategoryResponse` |
| 17 | DELETE | `/api/categories/{id}` | ADMIN | `204` |
| 18 | POST | `/api/orders` | CUSTOMER | `OrderResponse` |
| 19 | GET | `/api/orders/my` | CUSTOMER | `OrderResponse[]` |
| 20 | GET | `/api/orders/{id}` | Authentifié | `OrderResponse` |
| 21 | PUT | `/api/orders/{id}/cancel` | CUSTOMER | `OrderResponse` |
| 22 | PUT | `/api/orders/{id}/status` | ADMIN/SELLER | `OrderResponse` |
| 23 | GET | `/api/cart` | CUSTOMER | `CartResponse` |
| 24 | POST | `/api/cart/items` | CUSTOMER | `CartResponse` |
| 25 | PUT | `/api/cart/items/{itemId}` | CUSTOMER | `CartResponse` |
| 26 | DELETE | `/api/cart/items/{itemId}` | CUSTOMER | `CartResponse` |
| 27 | POST | `/api/cart/coupon` | CUSTOMER | `CartResponse` |
| 28 | DELETE | `/api/cart/coupon` | CUSTOMER | `CartResponse` |
| 29 | POST | `/api/coupons` | ADMIN | `CouponResponse` (201) |
| 30 | PUT | `/api/coupons/{id}` | ADMIN | `CouponResponse` |
| 31 | DELETE | `/api/coupons/{id}` | ADMIN | `204` |
| 32 | GET | `/api/coupons` | ADMIN | `CouponResponse[]` |
| 33 | GET | `/api/coupons/{id}` | Authentifié | `CouponResponse` |
| 34 | GET | `/api/coupons/code/{code}` | 🔓 | `CouponResponse` |
| 35 | GET | `/api/coupons/validate/{code}` | 🔓 | `boolean` |
| 36 | POST | `/api/reviews` | CUSTOMER | `ReviewResponse` (201) |
| 37 | PUT | `/api/reviews/{id}` | CUSTOMER | `ReviewResponse` |
| 38 | DELETE | `/api/reviews/{id}` | CUSTOMER | `204` |
| 39 | GET | `/api/reviews/{id}` | 🔓 | `ReviewResponse` |
| 40 | GET | `/api/reviews/product/{id}` | 🔓 | `ReviewResponse[]` |
| 41 | GET | `/api/reviews/user/{id}` | 🔓 | `ReviewResponse[]` |
| 42 | GET | `/api/dashboard/admin` | ADMIN | `AdminDashboard` |
| 43 | GET | `/api/dashboard/seller` | SELLER | `SellerDashboard` |
| 44 | GET | `/api/dashboard/customer` | CUSTOMER | `CustomerDashboard` |

---

## 10. Intercepteur HTTP Angular (à implémenter)

Pour attacher automatiquement le token JWT à chaque requête :

```typescript
// src/app/interceptors/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    const cloned = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    return next(cloned);
  }
  return next(req);
};
```

```typescript
// app.config.ts
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(withInterceptors([authInterceptor]))
  ]
};
```

---

## 11. Variables d'environnement Angular

```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

Usage dans les services :
```typescript
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private base = `${environment.apiUrl}/products`;
  // ...
}
```
