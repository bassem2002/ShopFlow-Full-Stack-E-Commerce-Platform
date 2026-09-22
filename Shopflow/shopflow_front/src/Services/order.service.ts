import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';
import { environment } from '../environments/environment';
import { OrderRequest, OrderResponse } from '../Modeles/Order';
import { OrderStatus } from '../Modeles/enums';

// ============================================================
// Service : OrderService
// Description : Passer et suivre les commandes
// Endpoint : /api/orders
// ============================================================

@Injectable({ providedIn: 'root' })
export class OrderService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/orders`;

  /** Récupérer toutes les commandes (ADMIN) */
  getAllOrders(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(this.base).pipe(
      catchError(err => { console.error('Erreur toutes les commandes:', err); throw err; })
    );
  }

  /** Passer une commande depuis le panier (CUSTOMER) */
  placeOrder(request: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(this.base, request).pipe(
      catchError(err => { console.error('Erreur passage commande:', err); throw err; })
    );
  }

  /** Historique de mes commandes (CUSTOMER) */
  getMyOrders(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.base}/my`).pipe(
      catchError(err => { console.error('Erreur mes commandes:', err); throw err; })
    );
  }

  /** Détail d'une commande */
  getOrderById(id: number): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.base}/${id}`).pipe(
      catchError(err => { console.error(`Erreur commande ${id}:`, err); throw err; })
    );
  }

  /** Annuler une commande (CUSTOMER — seulement si PENDING) */
  cancelOrder(id: number): Observable<OrderResponse> {
    return this.http.put<OrderResponse>(`${this.base}/${id}/cancel`, {}).pipe(
      catchError(err => { console.error(`Erreur annulation commande ${id}:`, err); throw err; })
    );
  }

  /** Changer le statut d'une commande (ADMIN / SELLER) */
  updateOrderStatus(id: number, status: OrderStatus): Observable<OrderResponse> {
    const params = new HttpParams().set('status', status);
    return this.http.put<OrderResponse>(`${this.base}/${id}/status`, {}, { params }).pipe(
      catchError(err => { console.error(`Erreur statut commande ${id}:`, err); throw err; })
    );
  }
}
