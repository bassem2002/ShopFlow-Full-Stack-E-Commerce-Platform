# ShopFlow — Backend Spec · Partie 1 : Modèles de Données

> **Stack** : Spring Boot 3.5 · Java 21 · JPA/Hibernate · H2 (dev) · Lombok  
> **Package racine** : `com.shopflow.shopflow`

---

## 1. Énumérations (Enums)

### `Role`
```java
// package: enums
public enum Role {
    ADMIN,
    SELLER,
    CUSTOMER
}
```
| Valeur     | Description                        |
|------------|------------------------------------|
| `ADMIN`    | Administrateur — accès total       |
| `SELLER`   | Vendeur — gère ses produits        |
| `CUSTOMER` | Client — achète et laisse des avis |

**Mapping TypeScript :**
```typescript
export type Role = 'ADMIN' | 'SELLER' | 'CUSTOMER';
```

---

### `OrderStatus`
```java
// package: enums
public enum OrderStatus {
    PENDING, PAID, PROCESSING,
    SHIPPED, DELIVERED, CANCELLED, REFUNDED
}
```
**Mapping TypeScript :**
```typescript
export type OrderStatus =
  'PENDING' | 'PAID' | 'PROCESSING' |
  'SHIPPED' | 'DELIVERED' | 'CANCELLED' | 'REFUNDED';
```

---

### `CouponType`
```java
// package: enums
public enum CouponType {
    PERCENT,  // réduction en % (ex: 10%)
    FIXED     // réduction fixe (ex: 20 DT)
}
```
**Mapping TypeScript :**
```typescript
export type CouponType = 'PERCENT' | 'FIXED';
```

---

## 2. Entities JPA

### 2.1 `User` — Table `users`

```java
@Entity @Table(name = "users")
public class User implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;                    // utilisé comme username Spring Security

    @Column(nullable = false)
    private String password;                 // hashé BCrypt

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;                       // ADMIN | SELLER | CUSTOMER

    @Column(nullable = false)
    private Boolean active;                  // soft-disable du compte

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
```

| Champ       | Type Java        | Type SQL        | Contrainte          | Type Angular  |
|-------------|------------------|-----------------|---------------------|---------------|
| `id`        | `Long`           | `BIGINT`        | PK, auto-increment  | `number`      |
| `email`     | `String`         | `VARCHAR`       | UNIQUE, NOT NULL    | `string`      |
| `password`  | `String`         | `VARCHAR`       | NOT NULL (BCrypt)   | *(jamais exposé)* |
| `firstName` | `String`         | `VARCHAR`       | nullable            | `string`      |
| `lastName`  | `String`         | `VARCHAR`       | nullable            | `string`      |
| `role`      | `Role` (enum)    | `VARCHAR`       | NOT NULL            | `Role`        |
| `active`    | `Boolean`        | `BOOLEAN`       | NOT NULL            | `boolean`     |
| `createdAt` | `LocalDateTime`  | `TIMESTAMP`     | NOT NULL            | `Date`        |

> **⚠️ Mapping critique** : `LocalDateTime` (Java) → `string` ISO-8601 dans le JSON → `Date` ou `string` en Angular.  
> Utiliser `new Date(response.createdAt)` côté Angular.

---

### 2.2 `Category` — Table `categories`

```java
@Entity @Table(name = "categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    // Auto-référence : catégorie parente (nullable = racine)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // Sous-catégories enfants
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Category> children = new ArrayList<>();
}
```

| Champ         | Type Java          | Relation JPA      | Type Angular           |
|---------------|--------------------|-------------------|------------------------|
| `id`          | `Long`             | —                 | `number`               |
| `name`        | `String`           | —                 | `string`               |
| `description` | `String`           | —                 | `string \| null`       |
| `parent`      | `Category`         | `@ManyToOne`      | `number` (parentId)    |
| `children`    | `List<Category>`   | `@OneToMany`      | `CategoryResponse[]`   |

**Relation clé :** Une catégorie peut être hiérarchique (ex : `Vêtements > T-shirts > Oversize`).

---

### 2.3 `Product` — Table `products`

```java
@Entity @Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private Double price;

    private Double promoPrice;       // null = pas de promo

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Boolean active;          // soft-delete

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Relation ManyToMany via table "product_categories"
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_categories",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    // Variantes (tailles, couleurs…)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    // URLs d'images stockées dans "product_images"
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();
}
```

| Champ         | Type Java         | Relation JPA         | Type Angular           |
|---------------|-------------------|----------------------|------------------------|
| `id`          | `Long`            | —                    | `number`               |
| `seller`      | `User`            | `@ManyToOne`         | `number` (sellerId)    |
| `name`        | `String`          | —                    | `string`               |
| `description` | `String`          | —                    | `string`               |
| `price`       | `Double`          | —                    | `number`               |
| `promoPrice`  | `Double`          | —                    | `number \| null`       |
| `stock`       | `Integer`         | —                    | `number`               |
| `active`      | `Boolean`         | —                    | `boolean`              |
| `createdAt`   | `LocalDateTime`   | —                    | `Date`                 |
| `categories`  | `List<Category>`  | `@ManyToMany`        | `number[]` (ids)       |
| `variants`    | `List<ProductVariant>` | `@OneToMany`    | `ProductVariant[]`     |
| `images`      | `List<String>`    | `@ElementCollection` | `string[]`             |

---

### 2.4 `ProductVariant` — Table `product_variants`

```java
@Entity @Table(name = "product_variants")
public class ProductVariant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String attribute;       // ex: "Size", "Color"

    @Column(name = "variant_value", nullable = false, length = 100)
    private String value;           // ex: "M", "Rouge"

    @Column(nullable = false)
    private Integer stockAdditional; // stock propre à cette variante

    @Column(nullable = false)
    private Double priceDelta;       // +/- par rapport au prix de base

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
```

| Champ             | Type Java  | Type Angular   | Note                       |
|-------------------|------------|----------------|----------------------------|
| `id`              | `Long`     | `number`       |                            |
| `attribute`       | `String`   | `string`       | "Size", "Color"…           |
| `value`           | `String`   | `string`       | "XL", "Bleu"…              |
| `stockAdditional` | `Integer`  | `number`       | stock spécifique           |
| `priceDelta`      | `Double`   | `number`       | ex: +5.0 → prix final = base + delta |

---

### 2.5 `Order` — Table `orders`

```java
@Entity @Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;      // PENDING → PAID → SHIPPED → DELIVERED

    @Column(nullable = false, unique = true)
    private String orderNumber;      // format: ORD-2026-XXXXX

    @Column(nullable = false)
    private String shippingAddress;

    private Double subTotal;
    private Double shippingCost;
    private Double total;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}
```

| Champ             | Type Java        | Type Angular     |
|-------------------|------------------|------------------|
| `id`              | `Long`           | `number`         |
| `customer`        | `User`           | `number` (id)    |
| `status`          | `OrderStatus`    | `OrderStatus`    |
| `orderNumber`     | `String`         | `string`         |
| `shippingAddress` | `String`         | `string`         |
| `subTotal`        | `Double`         | `number`         |
| `shippingCost`    | `Double`         | `number`         |
| `total`           | `Double`         | `number`         |
| `createdAt`       | `LocalDateTime`  | `Date`           |
| `items`           | `List<OrderItem>`| `OrderItem[]`    |

---

### 2.6 `OrderItem` — Table `order_items`

```java
@Entity @Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductVariant variant;   // nullable — variante optionnelle

    private Integer quantity;
    private Double unitPrice;         // prix snapshot au moment de l'achat
}
```

| Champ       | Type Java        | Relation JPA   | Type Angular        |
|-------------|------------------|----------------|---------------------|
| `id`        | `Long`           | —              | `number`            |
| `order`     | `Order`          | `@ManyToOne`   | *(interne)*         |
| `product`   | `Product`        | `@ManyToOne`   | `number` (id)       |
| `variant`   | `ProductVariant` | `@ManyToOne`   | `number \| null`    |
| `quantity`  | `Integer`        | —              | `number`            |
| `unitPrice` | `Double`         | —              | `number`            |

---

### 2.7 `Cart` — Table `carts`

```java
@Entity @Table(name = "carts")
public class Cart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private User customer;           // 1 client = 1 panier unique

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;           // coupon appliqué (nullable)

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

| Champ       | Type Java        | Relation JPA   | Type Angular     |
|-------------|------------------|----------------|------------------|
| `id`        | `Long`           | —              | `number`         |
| `customer`  | `User`           | `@OneToOne`    | `number` (id)    |
| `items`     | `List<CartItem>` | `@OneToMany`   | `CartItem[]`     |
| `coupon`    | `Coupon`         | `@ManyToOne`   | `string \| null` (code) |
| `updatedAt` | `LocalDateTime`  | —              | `Date`           |

---

### 2.8 `CartItem` — Table `cart_items`

```java
@Entity @Table(name = "cart_items")
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;  // nullable

    @Column(nullable = false)
    private Integer quantity;
}
```

---

### 2.9 `Coupon` — Table `coupons`

```java
@Entity @Table(name = "coupons")
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;             // ex: "SUMMER10"

    @Enumerated(EnumType.STRING)
    private CouponType type;         // PERCENT | FIXED

    @Column(name = "coupon_value", nullable = false)
    private Double value;            // 10 = 10% ou 20 DT

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    private Integer maxUsages;
    private Integer currentUsages;

    @Column(nullable = false)
    private Boolean active;
}
```

| Champ            | Type Java       | Type Angular     |
|------------------|-----------------|------------------|
| `id`             | `Long`          | `number`         |
| `code`           | `String`        | `string`         |
| `type`           | `CouponType`    | `CouponType`     |
| `value`          | `Double`        | `number`         |
| `expirationDate` | `LocalDateTime` | `Date`           |
| `maxUsages`      | `Integer`       | `number`         |
| `currentUsages`  | `Integer`       | `number`         |
| `active`         | `Boolean`       | `boolean`        |

---

### 2.10 `Review` — Table `reviews`

```java
@Entity @Table(name = "reviews")
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating;          // 1 à 5

    @Column(length = 2000)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
```

---

## 3. DTOs — Request (données envoyées par Angular → API)

### `RegisterRequest`
```java
public class RegisterRequest {
    @NotBlank @Email  String email;
    @NotBlank         String password;
    @NotBlank         String firstName;
    @NotBlank         String lastName;
    Role role;                // CUSTOMER ou SELLER
    // Champs optionnels si role = SELLER :
    String storeName;
    String storeDescription;
    String storeLogo;
}
```

### `LoginRequest`
```java
public class LoginRequest {
    @NotBlank @Email  String email;
    @NotBlank         String password;
}
```

### `ProductRequest`
```java
public class ProductRequest {
    @NotNull          Long sellerId;
    @NotBlank         String name;
    @NotBlank         String description;
    @NotNull @Min(0)  Double price;
    Double promoPrice;         // nullable
    @NotNull @Min(0)  Integer stock;
    @NotEmpty         List<Long> categoryIds;
    List<String> images;
    @Valid List<ProductVariantRequest> variants;
}
```

### `ProductVariantRequest`
```java
public class ProductVariantRequest {
    String attribute;    // "Size", "Color"…
    String value;        // "XL", "Rouge"…
    Integer stockAdditional;
    Double priceDelta;
}
```

### `CategoryRequest`
```java
public class CategoryRequest {
    @NotBlank String name;
    String description;
    Long parentId;       // null = catégorie racine
}
```

### `OrderRequest`
```java
public class OrderRequest {
    String shippingAddress;
}
```

### `AddCartItemRequest`
```java
public class AddCartItemRequest {
    Long customerId;
    Long productId;
    Long variantId;    // nullable
    Integer quantity;
}
```

### `UpdateCartItemRequest`
```java
public class UpdateCartItemRequest {
    Integer quantity;
}
```

### `ApplyCouponRequest`
```java
public class ApplyCouponRequest {
    Long customerId;
    String couponCode;
}
```

### `ReviewRequest`
```java
public class ReviewRequest {
    Long productId;
    Integer rating;    // 1–5
    String comment;
}
```

### `CouponRequest`
```java
public class CouponRequest {
    String code;
    CouponType type;
    Double value;
    LocalDateTime expirationDate;
    Integer maxUsages;
}
```

---

## 4. DTOs — Response (données renvoyées par l'API → Angular)

### `AuthResponse`
```java
public class AuthResponse {
    String accessToken;   // JWT Bearer
    String refreshToken;
    String email;
    String role;          // "ADMIN" | "SELLER" | "CUSTOMER"
}
```
**TypeScript :**
```typescript
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  email: string;
  role: Role;
}
```

---

### `ProductResponse`
```java
public class ProductResponse {
    Long id;
    Long sellerId;
    String sellerEmail;
    String name;
    String description;
    Double price;
    Double promoPrice;
    Integer stock;
    Boolean active;
    LocalDateTime createdAt;
    List<Long> categoryIds;
    List<String> images;
    List<ProductVariantResponse> variants;
    Double averageRating;
}
```
**TypeScript :**
```typescript
export interface ProductResponse {
  id: number;
  sellerId: number;
  sellerEmail: string;
  name: string;
  description: string;
  price: number;
  promoPrice: number | null;
  stock: number;
  active: boolean;
  createdAt: string; // ISO 8601 → new Date(createdAt)
  categoryIds: number[];
  images: string[];
  variants: ProductVariantResponse[];
  averageRating: number | null;
}
```

---

### `ProductVariantResponse`
```typescript
export interface ProductVariantResponse {
  id: number;
  attribute: string;
  value: string;
  stockAdditional: number;
  priceDelta: number;
}
```

---

### `CategoryResponse`
```java
public class CategoryResponse {
    Long id;
    String name;
    String description;
    Long parentId;                    // null si catégorie racine
    List<CategoryResponse> children;  // récursif pour l'arbre
}
```
**TypeScript :**
```typescript
export interface CategoryResponse {
  id: number;
  name: string;
  description: string | null;
  parentId: number | null;
  children: CategoryResponse[];
}
```

---

### `OrderResponse`
```java
public class OrderResponse {
    Long id;
    String orderNumber;
    OrderStatus status;
    String shippingAddress;
    Double subTotal;
    Double shippingCost;
    Double total;
    LocalDateTime createdAt;
    List<OrderItemResponse> items;
}
```
**TypeScript :**
```typescript
export interface OrderResponse {
  id: number;
  orderNumber: string;
  status: OrderStatus;
  shippingAddress: string;
  subTotal: number;
  shippingCost: number;
  total: number;
  createdAt: string;
  items: OrderItemResponse[];
}
```

---

### `OrderItemResponse`
```typescript
export interface OrderItemResponse {
  productId: number;
  productName: string;
  variantId: number | null;
  variant: string | null;
  quantity: number;
  unitPrice: number;
}
```

---

### `CartResponse`
```java
public class CartResponse {
    Long cartId;
    Long customerId;
    List<CartItemResponse> items;
    String couponCode;
    Double subTotal;
    Double shippingFees;
    Double totalTTC;
}
```
**TypeScript :**
```typescript
export interface CartResponse {
  cartId: number;
  customerId: number;
  items: CartItemResponse[];
  couponCode: string | null;
  subTotal: number;
  shippingFees: number;
  totalTTC: number;
}
```

---

### `CartItemResponse`
```typescript
export interface CartItemResponse {
  itemId: number;
  productId: number;
  productName: string;
  variantId: number | null;
  variantLabel: string | null;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}
```

---

### `ReviewResponse`
```typescript
export interface ReviewResponse {
  id: number;
  productId: number;
  productName: string;
  userId: number;
  userName: string;
  rating: number;
  comment: string | null;
  createdAt: string;
}
```

---

### `CouponResponse`
```typescript
export interface CouponResponse {
  id: number;
  code: string;
  type: CouponType;
  value: number;
  expirationDate: string;
  maxUsages: number;
  currentUsages: number;
  active: boolean;
}
```

---

## 5. Schéma des Relations (vue globale)

```
User (1) ──────────── (N) Product        [seller]
User (1) ──────────── (1) Cart           [customer]
User (1) ──────────── (N) Order          [customer]
User (1) ──────────── (N) Review

Product (N) ──────── (N) Category        [product_categories]
Product (1) ──────── (N) ProductVariant
Product (1) ──────── (N) Review

Cart (1) ─────────── (N) CartItem
CartItem (N) ──────── (1) Product
CartItem (N) ──────── (1) ProductVariant [nullable]
Cart (N) ─────────── (1) Coupon          [nullable]

Order (1) ────────── (N) OrderItem
OrderItem (N) ──────── (1) Product
OrderItem (N) ──────── (1) ProductVariant [nullable]
```

---

## 6. Table de Mapping des Types Java → TypeScript

| Java                | JSON sérialisé       | TypeScript         | Note Angular                          |
|---------------------|----------------------|--------------------|---------------------------------------|
| `Long`              | `number`             | `number`           |                                       |
| `Integer`           | `number`             | `number`           |                                       |
| `Double`            | `number`             | `number`           |                                       |
| `String`            | `string`             | `string`           |                                       |
| `Boolean`           | `boolean`            | `boolean`          |                                       |
| `LocalDateTime`     | `"2026-04-22T10:30"` | `string` → `Date`  | `new Date(val)` ou pipe `date`        |
| `List<T>`           | `[...]`              | `T[]`              |                                       |
| `null` (nullable)   | `null`               | `T \| null`        |                                       |
| `Enum` (STRING)     | `"ADMIN"`            | `type` union       | ex: `'ADMIN' \| 'SELLER'`             |
| `Page<T>` (Spring)  | `{ content, totalElements, … }` | `Page<T>` | voir section API |
