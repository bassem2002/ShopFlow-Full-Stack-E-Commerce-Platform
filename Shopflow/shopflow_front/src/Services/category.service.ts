import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';
import { environment } from '../environments/environment';
import { CategoryRequest, CategoryResponse } from '../Modeles/Category';

// ============================================================
// Service : CategoryService
// Description : CRUD catégories + arbre hiérarchique
// Endpoint : /api/categories
// ============================================================

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/categories`;

  /** Liste plate de toutes les catégories */
  getAllCategories(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(this.base).pipe(
      catchError(err => { console.error('Erreur chargement catégories:', err); throw err; })
    );
  }

  /** Arbre hiérarchique (catégories imbriquées) */
  getCategoryTree(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(`${this.base}/tree`).pipe(
      catchError(err => { console.error('Erreur arbre catégories:', err); throw err; })
    );
  }

  /** Détail d'une catégorie */
  getCategoryById(id: number): Observable<CategoryResponse> {
    return this.http.get<CategoryResponse>(`${this.base}/${id}`).pipe(
      catchError(err => { console.error(`Erreur catégorie ${id}:`, err); throw err; })
    );
  }

  /** Créer une catégorie (ADMIN) */
  createCategory(category: CategoryRequest): Observable<CategoryResponse> {
    return this.http.post<CategoryResponse>(this.base, category).pipe(
      catchError(err => { console.error('Erreur création catégorie:', err); throw err; })
    );
  }

  /** Modifier une catégorie (ADMIN) */
  updateCategory(id: number, category: CategoryRequest): Observable<CategoryResponse> {
    return this.http.put<CategoryResponse>(`${this.base}/${id}`, category).pipe(
      catchError(err => { console.error(`Erreur mise à jour catégorie ${id}:`, err); throw err; })
    );
  }

  /** Supprimer une catégorie (ADMIN) */
  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`).pipe(
      catchError(err => { console.error(`Erreur suppression catégorie ${id}:`, err); throw err; })
    );
  }
}
