import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { CategoryComponent } from './category/Category.component';
import { OrderComponent } from './order/order.component';
import { ProductListComponent } from './product/product-list/product-list.component';
import { UserListComponent } from './user/user-list/user-list.component';
import { LoginComponent } from './components/login/login.component';
import { HomeComponent } from './home/home.component';

// ============================================================
// Routing ShopFlow — Angular 19
// Sécurité : authGuard protège les routes selon le rôle JWT
//
// Rôles : ADMIN | SELLER | CUSTOMER
// Stockage : localStorage (accessToken, role)
// ============================================================

export const appRoutes: Routes = [

  // ─── Racine ───────────────────────────────────────────────
  {
    path: '',
    component: HomeComponent,
    data: { title: 'Accueil — ShopFlow' },
  },
  {
    path: 'home',
    redirectTo: '',
    pathMatch: 'full',
  },

  // ─── Authentification (public) ────────────────────────────
  {
    path: 'login',
    component: LoginComponent,
    data: { title: 'Connexion — ShopFlow' },
  },
  {
    path: 'register',
    loadComponent: () => import('./register/register.component').then(m => m.RegisterComponent),
    data: { title: 'Inscription — ShopFlow' },
  },
  {
    path: 'forgot-password',
    loadComponent: () => import('./auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent),
    data: { title: 'Mot de passe oublié' }
  },
  {
    path: 'reset-password',
    loadComponent: () => import('./auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent),
    data: { title: 'Réinitialisation mot de passe' }
  },

  // ─── Accès refusé (public) ────────────────────────────────
  {
    path: 'forbidden',
    loadComponent: () =>
      import('./shared/forbidden/forbidden.component').then(m => m.ForbiddenComponent),
    data: { title: 'Accès refusé' },
  },

  // ─── Produits (public — lecture) ──────────────────────────
  {
    path: 'products',
    component: ProductListComponent,
    data: { title: 'Catalogue produits — ShopFlow' },
  },
  {
    path: 'products/new',
    canActivate: [authGuard],
    loadComponent: () => import('./product/product-form/product-form.component').then(m => m.ProductFormComponent),
    data: { title: 'Nouveau produit — ShopFlow', role: ['ADMIN', 'SELLER'] },
  },
  {
    path: 'products/edit/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./product/product-form/product-form.component').then(m => m.ProductFormComponent),
    data: { title: 'Modifier produit — ShopFlow', role: ['ADMIN', 'SELLER'] },
  },
  {
    path: 'products/:id',
    loadComponent: () => import('./product/product-detail/product-detail.component').then(m => m.ProductDetailComponent),
    data: { title: 'Détail produit — ShopFlow' },
  },

  // ─── Catégories (public — lecture) ───────────────────────
  {
    path: 'categories',
    component: CategoryComponent,
    data: { title: 'Catégories — ShopFlow' },
  },

  // ─── Panier (CUSTOMER uniquement) ────────────────────────
  {
    path: 'cart',
    canActivate: [authGuard],
    data: { title: 'Mon Panier', role: 'CUSTOMER' },
    loadComponent: () =>
      import('./cart/cart.component').then(m => m.CartComponent),
  },

  // ─── Commandes ────────────────────────────────────────────
  {
    path: 'orders',
    component: OrderComponent,
    canActivate: [authGuard],          // toute personne connectée
    data: { title: 'Commandes — ShopFlow' },
  },

  // ─── Utilisateurs (ADMIN uniquement) ─────────────────────
  {
    path: 'users',
    component: UserListComponent,
    canActivate: [authGuard],
    data: { title: 'Utilisateurs', role: 'ADMIN' },
  },

  // ─── Dashboard Admin ──────────────────────────────────────
  {
    path: 'dashboard/admin',
    canActivate: [authGuard],
    data: { title: 'Dashboard Admin', role: 'ADMIN' },
    loadComponent: () =>
      import('./dashboard/admin-dashboard/admin-dashboard.component')
        .then(m => m.AdminDashboardComponent),
    children: [
      { path: '', redirectTo: 'stats', pathMatch: 'full' },
      {
        path: 'stats',
        loadComponent: () => import('./dashboard/admin-dashboard/admin-stats/admin-stats.component').then(m => m.AdminStatsComponent)
      },
      {
        path: 'users',
        loadComponent: () => import('./user/user-list/user-list.component').then(m => m.UserListComponent)
      },
      {
        path: 'categories',
        loadComponent: () => import('./category/Category.component').then(m => m.CategoryComponent)
      }
    ]
  },

  // ─── Dashboard Vendeur ────────────────────────────────────
  {
    path: 'dashboard/seller',
    canActivate: [authGuard],
    data: { title: 'Dashboard Vendeur', role: 'SELLER' },
    loadComponent: () =>
      import('./seller-dashboard/seller-dashboard.component')
        .then(m => m.SellerDashboardComponent),
    children: [
      { path: '', redirectTo: 'stats', pathMatch: 'full' },
      {
        path: 'stats',
        loadComponent: () => import('./seller-dashboard/seller-stats/seller-stats.component').then(m => m.SellerStatsComponent)
      },
      {
        path: 'catalog',
        loadComponent: () => import('./seller-dashboard/seller-catalog/seller-catalog.component').then(m => m.SellerCatalogComponent)
      },
      {
        path: 'orders',
        loadComponent: () => import('./seller-dashboard/seller-orders/seller-orders.component').then(m => m.SellerOrdersComponent)
      }
    ]
  },

  // ─── Dashboard Client ─────────────────────────────────────
  {
    path: 'dashboard/customer',
    canActivate: [authGuard],
    data: { title: 'Mon Espace', role: 'CUSTOMER' },
    loadComponent: () =>
      import('./dashboard/customer-dashboard/customer-dashboard.component')
        .then(m => m.CustomerDashboardComponent),
  },

  // ─── Profil (tous les utilisateurs connectés) ───────────
  {
    path: 'profile',
    canActivate: [authGuard],
    data: { title: 'Mon Profil — ShopFlow' },
    loadComponent: () => import('./components/profile/profile.component').then(m => m.ProfileComponent),
  },

  // Wildcard — doit rester en dernier
  {
    path: '**',
    redirectTo: 'products',
  },
];
