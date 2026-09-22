# 🛒 ShopFlow

**Full-Stack Multi-Vendor E-Commerce Platform**

ShopFlow is a modern full-stack e-commerce web application built with **Spring Boot 3.5**, **Angular 19**, and **PostgreSQL**.

The platform supports three distinct roles — **Admin, Seller, and Customer** — and provides product catalog management, product variants, persistent shopping carts, coupons, order processing, inventory management, dashboards, reviews, and dual authentication using **JWT and Firebase Authentication**.

---

## ✨ Overview

ShopFlow was designed as a complete e-commerce platform with a clear separation between the frontend, backend, business logic, persistence layer, and security infrastructure.

The application follows a **client-server layered architecture**:

```text
Angular 19 SPA
      │
      │ HTTP / REST
      │ Bearer JWT / Firebase Token
      ▼
Spring Boot 3.5 Backend
      │
      ▼
Controllers
      │
      ▼
Services
      │
      ▼
Repositories
      │
      ▼
Spring Data JPA / Hibernate
      │
      ▼
Relational Database
 H2 Dev / PostgreSQL Prod
```

---

# 🚀 Main Features

## 👤 Multi-Role Platform

ShopFlow implements three application roles:

### ADMIN

Administrators can manage:

- Users
- Categories
- Coupons
- Platform statistics
- Administrative dashboards
- Catalog-related operations

### SELLER

Sellers can:

- Create products
- Update products
- Manage product variants
- Manage product inventory
- View seller orders
- Access seller dashboard statistics

### CUSTOMER

Customers can:

- Browse products
- View product details
- Add products or variants to cart
- Update cart quantities
- Apply discount coupons
- Place orders
- Cancel eligible orders
- View order history
- Submit product reviews

---

# 🛍️ E-Commerce Workflow

The main customer workflow is:

```text
Browse Products
      ↓
Product Details
      ↓
Choose Product / Variant
      ↓
Add to Cart
      ↓
Update Quantities
      ↓
Apply Coupon
      ↓
Checkout
      ↓
Validate Stock
      ↓
Create Order
      ↓
Update Inventory
      ↓
Clear Cart
```

ShopFlow verifies product inventory during checkout and updates stock when an order is created.

Order cancellation can restore product inventory when applicable.

---

# 📦 Product Catalog

The catalog supports:

- Product creation and modification
- Multiple product categories
- Hierarchical categories
- Product images
- Promotional pricing
- Product availability
- Seller ownership
- Product reviews
- Product variants
- Variant-specific stock
- Variant-specific price adjustments

Example product model:

```text
Product
│
├── Name
├── Description
├── Price
├── Promotional Price
├── Stock
├── Images
├── Categories
├── Seller
│
└── Variants
    ├── Attribute
    ├── Value
    ├── Price Delta
    └── Additional Stock
```

---

# 🛒 Shopping Cart

Each customer has a persistent shopping cart stored in the database.

The cart supports:

- Add item
- Remove item
- Update quantity
- Product variants
- Coupon application
- Price calculation
- Persistent database storage

Main entities:

```text
Cart
 ├── Customer
 ├── CartItem[]
 ├── Coupon
 └── UpdatedAt
```

---

# 🎟️ Coupons

ShopFlow supports discount coupons with:

- Coupon code
- Fixed discounts
- Percentage discounts
- Expiration date
- Activation status
- Usage limits
- Usage tracking

Supported types:

```text
PERCENT
FIXED
```

---

# 📦 Order Management

Orders contain:

- Unique order number
- Customer
- Shipping address
- Order items
- Subtotal
- Shipping cost
- Final total
- Order status
- Creation date

Order processing is executed inside a Spring transactional boundary to keep:

```text
Stock Update
      +
Order Creation
      +
Cart Cleanup
```

consistent within the same database transaction.

---

# 💳 Payment

A real external payment gateway is **not currently integrated**.

The project does not currently use Stripe, PayPal, or another payment processor.

Orders are created directly with a pending status after checkout.

Payment integration is therefore considered a future improvement.

---

# 🔐 Authentication

ShopFlow supports two authentication methods.

## 1. Local Authentication

```text
Email + Password
      ↓
Spring Security
      ↓
BCrypt Verification
      ↓
JWT Access Token
      +
Refresh Token
```

Passwords are hashed using:

```text
BCryptPasswordEncoder
```

---

## 2. Firebase Authentication

Users can also authenticate through Firebase.

```text
Angular
   ↓
Firebase Authentication
   ↓
Firebase ID Token
   ↓
Spring Boot
   ↓
Firebase Admin SDK
   ↓
Token Verification
   ↓
Application JWT
```

---

# 🔑 JWT Authentication

After successful authentication, ShopFlow generates:

```text
Access Token
→ JWT
→ 24-hour expiration

Refresh Token
→ Database backed
→ 7-day expiration
```

The frontend attaches the access token using an Angular HTTP interceptor:

```text
Authorization: Bearer <token>
```

When an HTTP `401` response is received, the frontend can attempt to refresh the access token.

---

# 🛡️ Role-Based Access Control

Authorization is enforced using **Spring Security RBAC**.

```text
ADMIN
SELLER
CUSTOMER
```

Example authorization model:

| Resource | Public | Customer | Seller | Admin |
|---|:---:|:---:|:---:|:---:|
| Browse products | ✅ | ✅ | ✅ | ✅ |
| Product details | ✅ | ✅ | ✅ | ✅ |
| Manage cart | ❌ | ✅ | ❌ | ❌ |
| Place order | ❌ | ✅ | ❌ | ❌ |
| Create product | ❌ | ❌ | ✅ | ✅ |
| Manage categories | ❌ | ❌ | ❌ | ✅ |
| Manage coupons | ❌ | ❌ | ❌ | ✅ |
| User administration | ❌ | ❌ | ❌ | ✅ |

Authorization is implemented through Spring Security configuration and method-level access control.

---

# 🏗️ Software Architecture

ShopFlow uses a **Layered Monolithic Architecture**.

```text
HTTP Request
     │
     ▼
┌─────────────────────┐
│     Controller      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│       Service       │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│     Repository      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   JPA / Hibernate   │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│     PostgreSQL      │
│       / H2          │
└─────────────────────┘
```

### Controller Layer

Responsible for:

- HTTP endpoints
- Request validation
- Request parameters
- DTO input/output
- HTTP responses

### Service Layer

Responsible for:

- Business logic
- Price calculations
- Inventory management
- Cart operations
- Order processing
- Transactions
- Security-related business rules

### Repository Layer

Built using Spring Data JPA:

```java
JpaRepository<Entity, Long>
```

It abstracts persistence operations from the business layer.

### DTO Layer

Request and response DTOs separate REST API contracts from internal JPA entities.

### Mapper Layer

Dedicated mapping logic is used for selected domain objects such as orders.

---

# 🧩 Design Patterns

Several software design and enterprise patterns are present in ShopFlow.

## GoF Patterns

### Builder Pattern

Used through Lombok `@Builder` for constructing entities and DTO objects.

```text
Object
  ↓
Builder
  ↓
Configured Instance
```

### Chain of Responsibility

Spring Security uses a request-processing filter chain involving authentication filters.

```text
HTTP Request
      ↓
Firebase Authentication Filter
      ↓
JWT Authentication Filter
      ↓
Spring Security Filter Chain
      ↓
Controller
```

---

## Enterprise & Architectural Patterns

### Repository Pattern

Persistence is abstracted using Spring Data repositories.

```text
Service
   ↓
Repository
   ↓
JPA / Hibernate
   ↓
Database
```

### Service Layer

Business workflows are encapsulated inside dedicated service classes.

### DTO Pattern

Dedicated request and response DTOs isolate REST contracts from persistence entities.

### Data Mapper

Mapping logic transforms entities into API response objects.

### Dependency Injection

Spring IoC manages dependencies between:

```text
Controller
Service
Repository
Security Components
Configuration
```

---

# 🧱 SOLID Principles

The architecture applies several SOLID principles.

### Single Responsibility Principle

Domain logic is separated into dedicated services such as:

```text
ProductService
CartService
OrderService
CouponService
ReviewService
```

Some services can still be further decomposed as the application evolves.

### Open/Closed Principle

Service abstractions allow implementations to evolve without changing controller contracts.

### Liskov Substitution Principle

Service implementations implement dedicated service interfaces.

### Interface Segregation Principle

Business capabilities are divided into focused domain service interfaces.

### Dependency Inversion Principle

Controllers generally depend on service abstractions rather than implementing business logic directly.

---

# 🗃️ Domain Model

The backend contains several interconnected JPA entities.

```text
User
├── SellerProfile
├── Product
├── Order
├── Cart
└── Review

Product
├── Categories
├── ProductVariants
├── Reviews
└── Images

Cart
├── CartItems
└── Coupon

Order
└── OrderItems
```

Main entities include:

```text
User
SellerProfile
Product
ProductVariant
Category
Cart
CartItem
Order
OrderItem
Coupon
Review
RefreshToken
PasswordResetToken
```

---

# 🗄️ Database

ShopFlow supports two database environments.

## Development

```text
H2 File Database
```

## Production

```text
PostgreSQL
```

Persistence is handled using:

- Spring Data JPA
- Hibernate
- HikariCP

The application currently relies on Hibernate schema management rather than Flyway or Liquibase migrations.

---

# 🎨 Frontend Architecture

The frontend is built with **Angular 19** and uses modern Angular architecture.

Main technologies include:

```text
Angular 19
TypeScript
Standalone Components
Angular Signals
RxJS
Angular Material
TailwindCSS
Functional Guards
Functional HTTP Interceptors
Lazy Loading
```

---

# ⚡ Angular Signals

Reactive authentication state is managed using Angular Signals.

Examples include state for:

```text
Role
Email
User ID
Authentication Status
```

This provides lightweight reactive state without requiring NgRx.

---

# 🧭 Routing

Angular routes use lazy component loading where applicable.

```typescript
loadComponent(...)
```

Protected routes use functional guards to verify:

- Authentication
- User role
- Route permissions

---

# 🔄 HTTP Interceptor

The authentication interceptor:

1. Reads the JWT access token.
2. Adds it to outgoing HTTP requests.
3. Detects `401 Unauthorized` responses.
4. Attempts token refresh.
5. Retries the failed request when refresh succeeds.
6. Clears authentication state if refresh fails.

---

# 🌐 REST API

The backend exposes REST APIs for:

```text
Authentication
Users
Products
Categories
Cart
Orders
Coupons
Reviews
Dashboards
```

Example endpoints:

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/firebase
POST /api/auth/refresh

GET  /api/products
GET  /api/products/{id}
POST /api/products
PUT  /api/products/{id}

GET  /api/cart
POST /api/cart/items
POST /api/cart/coupon

POST /api/orders
GET  /api/orders/my

GET  /api/categories/tree
POST /api/categories

POST /api/coupons
```

---

# 📚 API Documentation

ShopFlow integrates **Swagger / OpenAPI** through SpringDoc.

The API documentation can be accessed through Swagger UI when the backend is running.

```text
/swagger-ui.html
```

---

# 🐳 Docker

The Spring Boot backend includes a multi-stage Docker build.

```text
Maven Build Stage
       ↓
Java 21 JRE Runtime
       ↓
Spring Boot JAR
       ↓
Port 8080
```

The current project does not include Docker Compose.

---

# 🛠️ Technology Stack

## Backend

| Technology | Purpose |
|---|---|
| Java 21 | Main backend language |
| Spring Boot 3.5 | Backend application framework |
| Spring MVC | REST APIs |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Persistence abstraction |
| Hibernate | ORM |
| JJWT | JWT creation and validation |
| Firebase Admin SDK | Firebase token verification |
| Lombok | Boilerplate reduction |
| Spring Validation | Request validation |
| SpringDoc OpenAPI | API documentation |

## Frontend

| Technology | Purpose |
|---|---|
| Angular 19 | SPA framework |
| TypeScript | Frontend language |
| Angular Signals | Reactive state |
| RxJS | Reactive programming |
| Angular Material | UI components |
| TailwindCSS | Styling |
| Angular Router | Navigation and route protection |
| Firebase | Social authentication |

## Database

| Environment | Database |
|---|---|
| Development | H2 |
| Production | PostgreSQL |

---

# 📂 Project Structure

```text
Shopflow/
│
├── shopflow/
│   │
│   ├── pom.xml
│   ├── Dockerfile
│   │
│   └── src/
│       ├── main/
│       │   ├── java/com/shopflow/shopflow/
│       │   │
│       │   ├── config/
│       │   ├── controller/
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   └── response/
│       │   ├── entity/
│       │   ├── enums/
│       │   ├── exception/
│       │   ├── mapper/
│       │   ├── repository/
│       │   ├── security/
│       │   └── service/
│       │       └── impl/
│       │
│       └── resources/
│
└── shopflow_front/
    │
    ├── package.json
    ├── angular.json
    ├── tailwind.config.js
    │
    └── src/
        ├── app/
        │   ├── auth/
        │   ├── cart/
        │   ├── category/
        │   ├── components/
        │   ├── dashboard/
        │   ├── guards/
        │   ├── interceptors/
        │   ├── order/
        │   ├── product/
        │   ├── register/
        │   ├── seller-dashboard/
        │   ├── shared/
        │   └── user/
        │
        ├── Modeles/
        ├── Services/
        └── environments/
```

---

# ⚙️ Configuration

Sensitive configuration should be provided through environment variables rather than committed directly to the repository.

Example:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

jwt.secret=${JWT_SECRET}
```

Firebase Admin credentials should also be provided securely outside the public repository.

Never commit:

```text
serviceAccountKey.json
Private Firebase credentials
JWT signing secrets
Database passwords
.env files containing secrets
```

---

# 🧪 Testing

Automated testing is currently an area for future improvement.

The project currently contains only the default Spring application context test and does not yet include a complete backend or frontend automated test suite.

Planned improvements include:

- JUnit unit tests
- Mockito service tests
- MockMvc controller tests
- Repository integration tests
- Angular component tests
- End-to-end tests

---

# 🔒 Security Considerations

The application architecture includes:

- BCrypt password hashing
- JWT authentication
- Refresh tokens
- Firebase authentication
- Stateless Spring Security
- Role-based access control
- Angular route guards
- HTTP authentication interceptor

Before deploying a public production instance, all sensitive credentials must be externalized and production security configuration should be reviewed.

---

# ⚠️ Current Limitations

The current implementation has several areas that can be improved:

- No real payment gateway
- No database migration framework such as Flyway or Liquibase
- Limited automated testing
- No distributed architecture
- No event-driven messaging
- No API Gateway
- No service discovery
- No Docker Compose environment
- Some backend classes could be further decomposed

These are intentional opportunities for future iterations rather than features currently claimed by the project.

---

# 🚀 Future Improvements

Possible extensions include:

- Stripe or PayPal payment integration
- Email-based password reset
- Flyway database migrations
- Full automated testing suite
- CI/CD pipeline
- Docker Compose environment
- Redis caching
- Product search improvements
- Audit logging
- Improved observability
- Production monitoring
- Cloud deployment
- File/object storage for product images

---

# 📸 Application Preview

Add screenshots here for:

```text
Landing Page
Product Catalog
Product Details
Shopping Cart
Checkout
Customer Orders
Seller Dashboard
Seller Product Management
Admin Dashboard
User Management
Category Management
```

Example structure:

```md
## Customer Experience

![Product Catalog](images/product-catalog.png)

![Shopping Cart](images/cart.png)

## Seller Dashboard

![Seller Dashboard](images/seller-dashboard.png)

## Admin Dashboard

![Admin Dashboard](images/admin-dashboard.png)
```

---

# 🎥 Demo

A video demonstration can be added here showing:

1. Customer registration and authentication
2. Product browsing
3. Shopping cart workflow
4. Coupon application
5. Order creation
6. Seller product management
7. Seller dashboard
8. Admin management interface

---

# 🎯 Concepts Demonstrated

ShopFlow demonstrates practical experience with:

- Full-stack web development
- REST API development
- Layered software architecture
- Object-relational mapping
- Spring Security
- JWT authentication
- Firebase authentication
- Role-based access control
- Repository Pattern
- Service Layer Pattern
- DTO Pattern
- Dependency Injection
- Transaction management
- Angular Signals
- Reactive frontend development
- Client-server architecture
- Relational database modeling
- Dockerized backend deployment

---

# 👨‍💻 Author

**Bassem Wali**

Software Engineering Student  
Full-Stack Development • Spring Boot • Angular • Applied AI
