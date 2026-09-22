import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface SellerStats {
  totalRevenue: number;
  totalProducts: number;
  pendingOrders: number;
  lowStockAlerts: { productName: string; currentStock: number }[];
  recentOrders: any[];
}

@Injectable({
  providedIn: 'root'
})
export class SellerService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  // Stats
  getDashboardStats(): Observable<SellerStats> {
    return this.http.get<SellerStats>(`${this.apiUrl}/dashboard/seller`);
  }

  // Catalogue
  createProduct(product: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/products`, product);
  }

  updateProduct(id: number, product: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/products/${id}`, product);
  }

  getProductsBySeller(sellerId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/products/seller/${sellerId}`);
  }

  // Commandes
  getSellerOrders(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/orders/seller`);
  }

  updateOrderStatus(orderId: number, status: string): Observable<any> {
    // Depending on backend, it might expect a string or an object.
    // Assuming standard REST for status update based on instructions.
    return this.http.put<any>(`${this.apiUrl}/orders/${orderId}/status?status=${status}`, {});
  }

  // Avis
  getProductReviews(productId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/reviews/product/${productId}`);
  }
}
