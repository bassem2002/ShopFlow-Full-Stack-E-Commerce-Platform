import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';
import { environment } from '../environments/environment';
import { ReviewRequest, ReviewResponse } from '../Modeles/Review';

// ============================================================
// Service : ReviewService
// Description : Avis et notes sur les produits
// Endpoint : /api/reviews
// ============================================================

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/reviews`;

  /** Tous les avis d'un produit (public) */
  getProductReviews(productId: number): Observable<ReviewResponse[]> {
    return this.http.get<ReviewResponse[]>(`${this.base}/product/${productId}`).pipe(
      catchError(err => { console.error(`Erreur avis produit ${productId}:`, err); throw err; })
    );
  }

  /** Tous les avis d'un utilisateur (public) */
  getUserReviews(userId: number): Observable<ReviewResponse[]> {
    return this.http.get<ReviewResponse[]>(`${this.base}/user/${userId}`).pipe(
      catchError(err => { console.error(`Erreur avis utilisateur ${userId}:`, err); throw err; })
    );
  }

  /** Détail d'un avis (public) */
  getReviewById(id: number): Observable<ReviewResponse> {
    return this.http.get<ReviewResponse>(`${this.base}/${id}`).pipe(
      catchError(err => { console.error(`Erreur avis ${id}:`, err); throw err; })
    );
  }

  /** Soumettre un avis (CUSTOMER) */
  createReview(userId: number, request: ReviewRequest): Observable<ReviewResponse> {
    return this.http.post<ReviewResponse>(`${this.base}?userId=${userId}`, request).pipe(
      catchError(err => { console.error('Erreur création avis:', err); throw err; })
    );
  }

  /** Modifier son avis (CUSTOMER) */
  updateReview(reviewId: number, userId: number, request: ReviewRequest): Observable<ReviewResponse> {
    return this.http.put<ReviewResponse>(`${this.base}/${reviewId}?userId=${userId}`, request).pipe(
      catchError(err => { console.error(`Erreur mise à jour avis ${reviewId}:`, err); throw err; })
    );
  }

  /** Supprimer son avis (CUSTOMER) */
  deleteReview(reviewId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${reviewId}?userId=${userId}`).pipe(
      catchError(err => { console.error(`Erreur suppression avis ${reviewId}:`, err); throw err; })
    );
  }
}
