# ShopFlow — Backend Spec · Partie 3 : Sécurité & Authentification JWT

> **Librairie JWT** : `io.jsonwebtoken` (JJWT) v0.11.5  
> **Algorithme** : `HS256` (HMAC-SHA-256)  
> **Session** : Stateless (pas de session côté serveur)  
> **Package sécurité** : `com.shopflow.shopflow.security`

---

## 1. Vue d'ensemble du flux d'authentification

```
┌─────────────┐          POST /api/auth/login           ┌──────────────┐
│   Angular   │ ──────────────────────────────────────► │ AuthController│
│  (Frontend) │     { email, password }                 └──────┬───────┘
│             │                                                │ vérifie credentials
│             │                                         ┌──────▼───────┐
│             │                                         │  AuthService  │
│             │                                         └──────┬───────┘
│             │                                                │ génère tokens
│             │                                         ┌──────▼───────┐
│             │ ◄──────────────────────────────────────│  JwtService   │
│             │   { accessToken, refreshToken,          └──────────────┘
│             │     email, role }
│             │
│             │  Requêtes suivantes :
│             │  Authorization: Bearer <accessToken>
│             │ ──────────────────────────────────────► ┌──────────────────────┐
│             │                                         │ JwtAuthenticationFilter│
│             │                                         └──────────┬───────────┘
│             │                                                    │ valide token
│             │                                         ┌──────────▼───────────┐
│             │ ◄───────────────────────────────────── │  Controller / Service │
└─────────────┘                                         └──────────────────────┘
```

---

## 2. Structure du Token JWT

### 2.1 Format du token

Un JWT est composé de 3 parties séparées par un `.` :

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNzE0NzM2MDAwLCJleHAiOjE3MTQ4MjI0MDB9.SIGNATURE
    ^── Header ──^         ^────────────── Payload ─────────────────^    ^─ Signature ─^
```

---

### 2.2 Header

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

---

### 2.3 Payload (Claims)

```json
{
  "sub": "john@example.com",      // subject = email de l'utilisateur (username Spring Security)
  "iat": 1714736000,               // issued at = timestamp de création (secondes Unix)
  "exp": 1714822400                // expiration = iat + 86400 (24 heures)
}
```

| Claim | Type Java | Description | Valeur exemple |
|-------|-----------|-------------|----------------|
| `sub` | `String` | Email de l'utilisateur | `"john@example.com"` |
| `iat` | `Date` | Date de génération | `new Date()` |
| `exp` | `Date` | Date d'expiration | `iat + 24h` |

> **Note :** Le rôle de l'utilisateur n'est **pas** dans le payload JWT.  
> Il est rechargé depuis la base de données via `UserDetailsService` à chaque requête.  
> Le role est retourné séparément dans `AuthResponse.role`.

---

### 2.4 Durées de vie

| Token | Durée | Stockage Angular recommandé |
|-------|-------|-----------------------------|
| `accessToken` | **24 heures** | `localStorage` ou mémoire |
| `refreshToken` | Persisté en base (`RefreshToken` entity) | `localStorage` |

---

### 2.5 Clé de signature

```java
// JwtService.java
private static final String SECRET_KEY =
    "ShopflowSecretKeyForJwtAuthenticationThatMustBeLongEnoughShopflowProject2026!";

private Key getSignInKey() {
    byte[] keyBytes = SECRET_KEY.getBytes();
    return Keys.hmacShaKeyFor(keyBytes);  // clé HMAC-SHA256
}
```

> ⚠️ **En production** : déplacer cette clé dans `application.properties` ou une variable d'environnement, jamais en dur dans le code.

---

## 3. Entité RefreshToken — Table `refresh_tokens`

```java
@Entity @Table(name = "refresh_tokens")
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;           // UUID aléatoire

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;              // 1 token par utilisateur

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private Boolean revoked;        // true = invalidé lors du logout
}
```

**Cycle de vie :**
1. **Login** → nouveau `RefreshToken` créé et sauvegardé en base
2. **Refresh** → vérifier que `token` existe, non révoqué, non expiré → générer nouveau `accessToken`
3. **Logout** → `revoked = true`

---

## 4. JwtService — Génération & Validation

```java
@Service
public class JwtService {

    // 1. Générer un token pour un utilisateur
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())   // email
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 86_400_000)) // 24h
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    // 2. Extraire l'email depuis le token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 3. Valider le token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // 4. Vérifier l'expiration
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
```

### Méthodes exposées

| Méthode | Entrée | Sortie | Description |
|---------|--------|--------|-------------|
| `generateToken(UserDetails)` | `UserDetails` | `String` | Génère un JWT signé HS256 valide 24h |
| `extractUsername(String)` | token JWT | `String` | Extrait le `sub` (email) |
| `isTokenValid(String, UserDetails)` | token + user | `boolean` | Vérifie signature + expiration + username |

---

## 5. JwtAuthenticationFilter — Étape par étape

Ce filtre s'exécute **une seule fois par requête HTTP** (hérite de `OncePerRequestFilter`).

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ÉTAPE 1 : Lire le header Authorization
        final String authHeader = request.getHeader("Authorization");

        // ÉTAPE 2 : Vérifier que le header commence par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // passer au filtre suivant sans authentifier
            return;
        }

        // ÉTAPE 3 : Extraire le token (supprimer "Bearer ")
        String jwt = authHeader.substring(7);

        // ÉTAPE 4 : Extraire l'email depuis le token
        String userEmail = jwtService.extractUsername(jwt);

        // ÉTAPE 5 : Vérifier qu'on n'est pas déjà authentifié
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // ÉTAPE 6 : Charger l'utilisateur depuis la base
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // ÉTAPE 7 : Valider le token (signature + expiration + username)
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // ÉTAPE 8 : Créer l'objet d'authentification Spring Security
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,          // principal (User entity)
                        null,                 // credentials (null car JWT)
                        userDetails.getAuthorities() // [ROLE_CUSTOMER]
                    );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ÉTAPE 9 : Stocker dans le SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // ÉTAPE 10 : Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}
```

### Résumé du flux par étape

```
Requête HTTP entrante
       │
       ▼
[1] Lire header "Authorization"
       │
       ├── absent ou pas "Bearer " ──► passer sans auth (routes publiques)
       │
       ▼
[2] Extraire le JWT (substring après "Bearer ")
       │
       ▼
[3] Extraire l'email (claims.sub)
       │
       ▼
[4] Déjà authentifié dans le SecurityContext ? ──► oui : passer
       │ non
       ▼
[5] Charger User depuis la BDD via UserDetailsService
       │
       ▼
[6] isTokenValid() → vérifier signature + expiration + email
       │
       ├── invalide ──► passer sans auth → Spring renverra 401
       │
       ▼
[7] Créer UsernamePasswordAuthenticationToken
[8] Stocker dans SecurityContextHolder
       │
       ▼
[9] Continuer → Controller s'exécute
```

---

## 6. SecurityConfig — Configuration Spring Security

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // active @PreAuthorize dans les controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF (inutile avec JWT stateless)
            .csrf(csrf -> csrf.disable())

            // Règles d'accès par URL
            .authorizeHttpRequests(auth -> auth

                // ── Routes publiques ──────────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(
                    "/h2-console/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()

                // GET publics : produits, catégories, avis
                .requestMatchers(HttpMethod.GET,
                    "/api/products/**",
                    "/api/categories/**",
                    "/api/reviews/**"
                ).permitAll()

                // ── Routes protégées par rôle ─────────────────────────────
                .requestMatchers("/api/categories/**").hasRole("ADMIN")
                .requestMatchers("/api/coupons/**").hasRole("ADMIN")
                .requestMatchers("/api/products/**").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers("/api/dashboard/admin").hasRole("ADMIN")
                .requestMatchers("/api/dashboard/seller").hasRole("SELLER")
                .requestMatchers("/api/dashboard/customer").hasRole("CUSTOMER")
                .requestMatchers("/api/cart/**").hasRole("CUSTOMER")
                .requestMatchers("/api/orders/**").hasAnyRole("CUSTOMER", "ADMIN", "SELLER")
                .requestMatchers("/api/reviews/**").hasRole("CUSTOMER")

                .anyRequest().authenticated()
            )

            // Pas de session HTTP côté serveur
            .sessionManagement(sess ->
                sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Provider d'authentification personnalisé
            .authenticationProvider(authenticationProvider)

            // Insérer le filtre JWT AVANT le filtre username/password standard
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // Autoriser H2 console (iframes)
            .headers(headers ->
                headers.frameOptions(f -> f.sameOrigin())
            );

        return http.build();
    }
}
```

### Tableau des règles de sécurité

| URL Pattern | Méthode HTTP | Rôle requis | Note |
|-------------|-------------|-------------|------|
| `/api/auth/**` | ALL | 🔓 Public | login, register, refresh, logout |
| `/api/products/**` | GET | 🔓 Public | lecture libre |
| `/api/products/**` | POST/PUT/DELETE | SELLER ou ADMIN | écriture |
| `/api/categories/**` | GET | 🔓 Public | lecture libre |
| `/api/categories/**` | POST/PUT/DELETE | ADMIN seulement | gestion |
| `/api/coupons/**` | ALL | ADMIN seulement | sauf validate/code (public) |
| `/api/cart/**` | ALL | CUSTOMER | panier personnel |
| `/api/orders/**` | ALL | CUSTOMER, SELLER, ADMIN | commandes |
| `/api/reviews/**` | GET | 🔓 Public | lecture libre |
| `/api/reviews/**` | POST/PUT/DELETE | CUSTOMER | écriture |
| `/api/dashboard/admin` | GET | ADMIN | |
| `/api/dashboard/seller` | GET | SELLER | |
| `/api/dashboard/customer` | GET | CUSTOMER | |
| `/swagger-ui/**` | ALL | 🔓 Public | docs API |

---

## 7. ApplicationConfig — Beans Spring Security

```java
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    // Charge l'utilisateur depuis la BDD par email
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // Provider qui utilise notre UserDetailsService + BCrypt
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Encodeur de mot de passe BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Manager utilisé dans AuthService.login()
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---

## 8. Gestion des rôles dans Spring Security

### Comment le rôle est transmis au SecurityContext

```java
// User.java implémente UserDetails
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    // ex: role = ADMIN → authority = "ROLE_ADMIN"
}
```

> **Convention Spring Security** : le préfixe `ROLE_` est ajouté automatiquement.  
> `hasRole("ADMIN")` vérifie `ROLE_ADMIN`.  
> `hasAuthority("ROLE_ADMIN")` est équivalent.

### Usage dans les controllers avec `@PreAuthorize`

```java
// Exemple dans CouponController
@PostMapping
@PreAuthorize("hasRole('ADMIN')")          // uniquement ADMIN
public ResponseEntity<CouponResponse> createCoupon(...) { ... }

// Exemple dans DashboardController
@GetMapping("/seller")
@PreAuthorize("hasRole('SELLER')")         // uniquement SELLER
public ResponseEntity<SellerDashboard> getSellerDashboard(...) { ... }
```

---

## 9. Flux complet Login → Requête protégée

### Étape 1 : Login Angular

```typescript
// auth.service.ts
login(credentials: LoginRequest): Observable<AuthResponse> {
  return this.http.post<AuthResponse>('/api/auth/login', credentials).pipe(
    tap(response => {
      localStorage.setItem('accessToken', response.accessToken);
      localStorage.setItem('refreshToken', response.refreshToken);
      localStorage.setItem('role', response.role);
      localStorage.setItem('email', response.email);
    })
  );
}
```

### Étape 2 : Intercepteur attache le token

```typescript
// auth.interceptor.ts (fonctionnel Angular 17+)
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    return next(req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    }));
  }
  return next(req);
};
```

### Étape 3 : Spring reçoit la requête

```
GET /api/orders/my
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

1. JwtAuthenticationFilter intercepte
2. Extrait email depuis le JWT
3. Charge User depuis la BDD
4. Valide signature HS256 + expiration
5. Injecte dans SecurityContextHolder
6. OrderController reçoit Authentication auth
7. User user = (User) auth.getPrincipal()
```

### Étape 4 : Refresh du token expiré

```typescript
// Intercepteur amélioré avec gestion 401
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('accessToken');
  const cloned = token
    ? req.clone({ headers: req.headers.set('Authorization', `Bearer ${token}`) })
    : req;

  return next(cloned).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Tenter un refresh
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
          return inject(AuthService).refresh({ refreshToken }).pipe(
            switchMap(response => {
              localStorage.setItem('accessToken', response.accessToken);
              return next(req.clone({
                headers: req.headers.set('Authorization', `Bearer ${response.accessToken}`)
              }));
            }),
            catchError(() => {
              // Refresh échoué → déconnecter
              localStorage.clear();
              inject(Router).navigate(['/login']);
              return throwError(() => error);
            })
          );
        }
      }
      return throwError(() => error);
    })
  );
};
```

---

## 10. Guard Angular — Protection des routes

```typescript
// src/app/guards/auth.guard.ts
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const token = localStorage.getItem('accessToken');
  const role  = localStorage.getItem('role');

  if (!token) {
    router.navigate(['/login']);
    return false;
  }

  // Vérifier le rôle requis si défini dans la route
  const requiredRole = route.data?.['role'] as string | undefined;
  if (requiredRole && role !== requiredRole) {
    router.navigate(['/forbidden']);
    return false;
  }

  return true;
};
```

**Usage dans `app.routes.ts` :**
```typescript
export const routes: Routes = [
  { path: 'login',     component: LoginComponent },
  { path: 'products',  component: ProductListComponent },
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canActivate: [authGuard],
    data: { role: 'ADMIN' }
  },
  {
    path: 'cart',
    component: CartComponent,
    canActivate: [authGuard],
    data: { role: 'CUSTOMER' }
  },
  {
    path: 'orders',
    component: OrderComponent,
    canActivate: [authGuard]   // toute personne connectée
  }
];
```

---

## 11. Résumé de l'architecture de sécurité

```
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Security Filter Chain                  │
│                                                                  │
│  Requête HTTP                                                    │
│      │                                                           │
│      ▼                                                           │
│  ┌─────────────────────────┐                                     │
│  │  JwtAuthenticationFilter│ ← extrait + valide le JWT           │
│  │  (OncePerRequestFilter) │ ← charge le User depuis BDD         │
│  │                         │ ← set SecurityContextHolder         │
│  └───────────┬─────────────┘                                     │
│              │                                                   │
│              ▼                                                   │
│  ┌─────────────────────────┐                                     │
│  │  SecurityFilterChain    │ ← vérifie les règles d'accès URL    │
│  │  (SecurityConfig)       │   + @PreAuthorize dans controllers   │
│  └───────────┬─────────────┘                                     │
│              │                                                   │
│              ▼                                                   │
│  ┌─────────────────────────┐                                     │
│  │  Controller / Service   │ ← accède à auth.getPrincipal()     │
│  └─────────────────────────┘                                     │
└─────────────────────────────────────────────────────────────────┘

Beans Spring Security :
  UserDetailsService  → charge User par email depuis UserRepository
  PasswordEncoder     → BCryptPasswordEncoder (hash mot de passe)
  AuthenticationProvider → DaoAuthenticationProvider
  AuthenticationManager  → utilisé dans AuthService.login()
```

---

## 12. Récapitulatif sécurité — Ce que Angular doit faire

| Action | Ce qu'Angular fait | Endpoint appelé |
|--------|-------------------|-----------------|
| Connexion | Stocker `accessToken` + `refreshToken` + `role` dans localStorage | `POST /api/auth/login` |
| Inscription | Idem login | `POST /api/auth/register` |
| Chaque requête | Intercepteur ajoute `Authorization: Bearer <token>` | Automatique |
| Token expiré (401) | Intercepteur appelle refresh, retry la requête | `POST /api/auth/refresh` |
| Déconnexion | Vider localStorage + invalider refresh en base | `POST /api/auth/logout` |
| Route protégée | `authGuard` vérifie token + rôle avant d'afficher | — |
| Afficher selon rôle | Lire `localStorage.getItem('role')` dans les composants | — |

---

## 13. Endpoints Auth — Récapitulatif complet

| Méthode | URL | Corps | Réponse | Note |
|---------|-----|-------|---------|------|
| POST | `/api/auth/register` | `RegisterRequest` | `AuthResponse` 201 | Crée compte + retourne tokens |
| POST | `/api/auth/login` | `LoginRequest` | `AuthResponse` 200 | Authentifie + retourne tokens |
| POST | `/api/auth/refresh` | `{ refreshToken }` | `AuthResponse` 200 | Renouvelle l'accessToken |
| POST | `/api/auth/logout` | `{ refreshToken }` | `204` | Révoque le refreshToken |
