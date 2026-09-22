import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';
import { environment } from '../environments/environment';
import { CouponRequest, CouponResponse } from '../Modeles/Coupon';

// ============================================================
// Service : CouponService
// Description : Gestion des coupons (ADMIN) + validation (public)
// Endpoint : /api/coupons
// ============================================================

@Injectable({ providedIn: 'root' })
export class CouponService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/coupons`;

  /** Liste de tous les coupons (ADMIN) */
  getAllCoupons(): Observable<CouponResponse[]> {
    return this.http.get<CouponResponse[]>(this.base).pipe(
      catchError(err => { console.error('Erreur liste coupons:', err); throw err; })
    );
  }

  /** Coupon par ID (Authentifié) */
  getCouponById(id: number): Observable<CouponResponse> {
    return this.http.get<CouponResponse>(`${this.base}/${id}`).pipe(
      catchError(err => { console.error(`Erreur coupon ${id}:`, err); throw err; })
    );
  }

  /** Coupon par code (public) */
  getCouponByCode(code: string): Observable<CouponResponse> {
    return this.http.get<CouponResponse>(`${this.base}/code/${code}`).pipe(
      catchError(err => { console.error(`Erreur coupon code ${code}:`, err); throw err; })
    );
  }

  /** Valider si un coupon est actif et non expiré (public) */
  validateCoupon(code: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.base}/validate/${code}`).pipe(
      catchError(err => { console.error(`Erreur validation coupon ${code}:`, err); throw err; })
    );
  }

  /** Créer un coupon (ADMIN) */
  createCoupon(request: CouponRequest): Observable<CouponResponse> {
    return this.http.post<CouponResponse>(this.base, request).pipe(
      catchError(err => { console.error('Erreur création coupon:', err); throw err; })
    );
  }

  /** Modifier un coupon (ADMIN) */
  updateCoupon(id: number, request: CouponRequest): Observable<CouponResponse> {
    return this.http.put<CouponResponse>(`${this.base}/${id}`, request).pipe(
      catchError(err => { console.error(`Erreur mise à jour coupon ${id}:`, err); throw err; })
    );
  }

  /** Supprimer un coupon (ADMIN) */
  deleteCoupon(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`).pipe(
      catchError(err => { console.error(`Erreur suppression coupon ${id}:`, err); throw err; })
    );
  }
}
