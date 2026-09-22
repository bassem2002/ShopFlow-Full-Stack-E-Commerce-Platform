import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, tap } from 'rxjs';
import { environment } from '../environments/environment';
import { AdminDashboard, CustomerDashboard, SellerDashboard } from '../Modeles/Dashboard';

// ============================================================
// Service : DashboardService
// Description : Statistiques par rôle
// Endpoint : /api/dashboard
// ============================================================

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/dashboard`;

  /** Dashboard administrateur — stats globales (ADMIN) */
  getAdminDashboard(): Observable<AdminDashboard> {
    return this.http.get<AdminDashboard>(`${this.base}/admin`).pipe(
      tap(data => console.log('DEBUG DASHBOARD - Raw Data:', data)),
      catchError(err => { console.error('Erreur dashboard admin:', err); throw err; })
    );
  }

  /** Dashboard vendeur — stats de ses produits (SELLER) */
  getSellerDashboard(): Observable<SellerDashboard> {
    return this.http.get<SellerDashboard>(`${this.base}/seller`).pipe(
      catchError(err => { console.error('Erreur dashboard vendeur:', err); throw err; })
    );
  }

  /** Dashboard client — historique et dépenses (CUSTOMER) */
  getCustomerDashboard(): Observable<CustomerDashboard> {
    return this.http.get<CustomerDashboard>(`${this.base}/customer`).pipe(
      catchError(err => { console.error('Erreur dashboard client:', err); throw err; })
    );
  }
}
