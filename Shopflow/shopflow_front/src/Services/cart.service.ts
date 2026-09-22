import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';
import { environment } from '../environments/environment';
import {
  AddCartItemRequest,
  ApplyCouponRequest,
  CartResponse,
  UpdateCartItemRequest,
} from '../Modeles/Cart';

// ============================================================
// Service : CartService
// Description : Gestion du panier (CUSTOMER uniquement)
// Endpoint : /api/cart
// ============================================================

@Injectable({ providedIn: 'root' })
export class CartService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/cart`;

  /** Récupérer le panier du client connecté */
  getCart(customerId?: number): Observable<CartResponse> {
    return this.http.get<CartResponse>(this.base).pipe(
      catchError(err => { console.error('Erreur chargement panier:', err); throw err; })
    );
  }

  /** Ajouter un article au panier */
  addItem(request: AddCartItemRequest): Observable<CartResponse> {
    return this.http.post<CartResponse>(`${this.base}/items`, request).pipe(
      catchError(err => { console.error('Erreur ajout panier:', err); throw err; })
    );
  }

  /** Modifier la quantité d'un article */
  updateItem(itemId: number, request: UpdateCartItemRequest): Observable<CartResponse> {
    return this.http.put<CartResponse>(`${this.base}/items/${itemId}`, request).pipe(
      catchError(err => { console.error(`Erreur mise à jour article ${itemId}:`, err); throw err; })
    );
  }

  /** Supprimer un article du panier */
  removeItem(itemId: number): Observable<CartResponse> {
    return this.http.delete<CartResponse>(`${this.base}/items/${itemId}`).pipe(
      catchError(err => { console.error(`Erreur suppression article ${itemId}:`, err); throw err; })
    );
  }

  /** Appliquer un coupon promo */
  applyCoupon(request: ApplyCouponRequest): Observable<CartResponse> {
    const payload = {
      code: request.code || request.couponCode || ''
    };
    return this.http.post<CartResponse>(`${this.base}/coupon`, payload).pipe(
      catchError(err => { console.error('Erreur application coupon:', err); throw err; })
    );
  }

  /** Retirer le coupon appliqué */
  removeCoupon(customerId?: number): Observable<CartResponse> {
    return this.http.delete<CartResponse>(`${this.base}/coupon`).pipe(
      catchError(err => { console.error('Erreur suppression coupon:', err); throw err; })
    );
  }
}
