import { Component, computed, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../Services/auth.service';

type UserRole = 'CUSTOMER' | 'SELLER' | 'ADMIN' | 'GUEST';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatBadgeModule,
    MatInputModule,
    MatFormFieldModule,
    FormsModule
  ],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  public router = inject(Router);
  private authService = inject(AuthService);

  // Signals pour gérer l'état réactif
  userRole = computed(() => (this.authService.currentRole() as UserRole) || 'GUEST');
  isMobileMenuOpen = signal(false);
  cartItemCount = signal(0);
  searchQuery = '';

  onSearch() {
    if (this.searchQuery.trim()) {
      this.router.navigate(['/products'], { queryParams: { search: this.searchQuery } });
    }
  }

  ngOnInit() {
    // Récupérer le nombre d'articles du panier
    const cart = localStorage.getItem('cart');
    if (cart) {
      const cartItems = JSON.parse(cart);
      this.cartItemCount.set(cartItems.length);
    }
  }

  // Navigation dynamique selon le rôle (basée sur vos routes)
  navLinks = computed(() => {
    switch (this.userRole()) {
      case 'ADMIN':
        return [
          { label: 'Accueil', path: '/', exact: true },
          { label: 'Tableau de bord', path: '/dashboard/admin/stats', exact: false },
          { label: 'Utilisateurs', path: '/users', exact: false },
          { label: 'Catégories', path: '/categories', exact: false },
          { label: 'Catalogue', path: '/products', exact: false },
          { label: 'Commandes', path: '/orders', exact: false }
        ];
      case 'SELLER':
        return [
          { label: 'Accueil', path: '/', exact: true },
          { label: 'Dashboard', path: '/dashboard/seller/stats', exact: false },
          { label: 'Catalogue', path: '/dashboard/seller/catalog', exact: false },
          { label: 'Commandes', path: '/dashboard/seller/orders', exact: false },
          { label: 'Boutique', path: '/products', exact: false }
        ];
      case 'CUSTOMER':
        return [
          { label: 'Accueil', path: '/', exact: true },
          { label: 'Boutique', path: '/products', exact: true },
          { label: 'Catégories', path: '/categories', exact: false },
          { label: 'Mes Commandes', path: '/orders', exact: false },
          { label: 'Mon Panier', path: '/cart', exact: false },
          { label: 'Mon Espace', path: '/dashboard/customer', exact: false }
        ];
      default: // GUEST
        return [
          { label: 'Accueil', path: '/', exact: true },
          { label: 'Boutique', path: '/products', exact: true },
          { label: 'Catégories', path: '/categories', exact: false }
        ];
    }
  });

  toggleMobileMenu() {
    this.isMobileMenuOpen.update(val => !val);
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        this.cartItemCount.set(0);
        this.router.navigate(['/']);
        if (this.isMobileMenuOpen()) {
          this.toggleMobileMenu();
        }
      },
      error: () => {
        // En cas d'erreur API, on vide quand même localement
        this.authService.clearSession();
        this.cartItemCount.set(0);
        this.router.navigate(['/']);
      }
    });
  }
}