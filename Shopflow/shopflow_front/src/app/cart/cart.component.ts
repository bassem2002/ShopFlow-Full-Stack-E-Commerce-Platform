import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../Services/cart.service';
import { CartResponse } from '../../Modeles/Cart';
import { AuthService } from '../../Services/auth.service';
import { Router } from '@angular/router';
import { OrderService } from '../../Services/order.service';

import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, RouterModule],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css'
})

export class CartComponent implements OnInit {
  private cartService = inject(CartService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private orderService = inject(OrderService);
  
  cart: CartResponse | null = null;
  loading = false;
  error: string | null = null;
  couponInput = '';

  private currentCustomerId: number | null = null; 

  ngOnInit(): void {
    this.currentCustomerId = this.authService.getUserId();
    if (!this.currentCustomerId) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadCart();
  }

  loadCart(): void {
    if (!this.currentCustomerId) return;
    this.loading = true;
    this.cartService.getCart(this.currentCustomerId).subscribe({
      next: (data) => {
        this.cart = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = "Erreur lors du chargement du panier.";
        this.loading = false;
        console.error(err);
      }
    });
  }

  updateQuantity(itemId: number, newQuantity: number): void {
    if (newQuantity < 1) return;
    this.cartService.updateItem(itemId, { quantity: newQuantity }).subscribe({
      next: (data) => this.cart = data,
      error: (err) => console.error(err)
    });
  }

  removeItem(itemId: number): void {
    this.cartService.removeItem(itemId).subscribe({
      next: (data) => this.cart = data,
      error: (err) => console.error(err)
    });
  }

  applyCoupon(): void {
    if (!this.couponInput.trim() || !this.currentCustomerId) return;
    this.cartService.applyCoupon({ customerId: this.currentCustomerId, couponCode: this.couponInput }).subscribe({
      next: (data) => {
        this.cart = data;
        this.couponInput = '';
      },
      error: (err) => {
        alert("Coupon invalide ou expiré.");
        console.error(err);
      }
    });
  }

  removeCoupon(): void {
    if (!this.currentCustomerId) return;
    this.cartService.removeCoupon(this.currentCustomerId).subscribe({
      next: (data) => this.cart = data,
      error: (err) => console.error(err)
    });
  }

  checkout(): void {
    if (!this.cart || this.cart.items.length === 0) return;
    
    // Pour simplifier, on envoie une adresse de livraison par défaut ou on demande à l'utilisateur.
    // Idéalement, il faudrait un formulaire modal ou une redirection vers une page de checkout complète.
    const address = prompt("Veuillez saisir votre adresse de livraison :", "123 Rue de Paris, France");
    if (!address) return; // Annulé par l'utilisateur

    this.loading = true;
    this.orderService.placeOrder({ shippingAddress: address }).subscribe({
      next: (order) => {
        alert(`Commande passée avec succès ! Numéro : ${order.orderNumber}`);
        // Rediriger vers l'historique des commandes
        this.router.navigate(['/orders']);
      },
      error: (err) => {
        this.error = "Erreur lors de la validation de la commande.";
        this.loading = false;
        console.error(err);
      }
    });
  }
}
