import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';
import { environment } from '../environments/environment';
import { ProductFilter, ProductRequest, ProductResponse } from '../Modeles/Product';
import { SpringPage } from '../Modeles/Pagination';

// ============================================================
// Service : ProductService
// Description : CRUD produits + pagination + recherche
// Endpoint : /api/products
// ============================================================

@Injectable({ providedIn: 'root' })
export class ProductService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/products`;

  /** Liste paginée avec filtres */
  getProducts(filters: ProductFilter = {}): Observable<SpringPage<ProductResponse>> {
    let params = new HttpParams();
    if (filters.categoryId !== undefined) params = params.set('categoryId', filters.categoryId);
    if (filters.minPrice   !== undefined) params = params.set('minPrice',   filters.minPrice);
    if (filters.maxPrice   !== undefined) params = params.set('maxPrice',   filters.maxPrice);
    if (filters.sellerId   !== undefined) params = params.set('sellerId',   filters.sellerId);
    if (filters.promo      !== undefined) params = params.set('promo',      filters.promo);
    if (filters.page       !== undefined) params = params.set('page',       filters.page);
    if (filters.size       !== undefined) params = params.set('size',       filters.size);
    if (filters.sortBy)                   params = params.set('sortBy',     filters.sortBy);
    if (filters.sortDirection)            params = params.set('sortDirection', filters.sortDirection);

    return this.http.get<SpringPage<ProductResponse>>(this.base, { params }).pipe(
      catchError(err => { console.error('Erreur produits:', err); throw err; })
    );
  }

  /** Détail d'un produit */
  getProductById(id: number): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.base}/${id}`).pipe(
      catchError(err => { console.error(`Erreur produit ${id}:`, err); throw err; })
    );
  }

  /** Recherche textuelle */
  searchProducts(q: string, page = 0, size = 10): Observable<SpringPage<ProductResponse>> {
    const params = new HttpParams().set('q', q).set('page', page).set('size', size);
    return this.http.get<SpringPage<ProductResponse>>(`${this.base}/search`, { params }).pipe(
      catchError(err => { console.error('Erreur recherche:', err); throw err; })
    );
  }

  /** Top produits les plus vendus */
  getTopSelling(): Observable<ProductResponse[]> {
    return this.http.get<ProductResponse[]>(`${this.base}/top-selling`).pipe(
      catchError(err => { console.error('Erreur top-selling:', err); throw err; })
    );
  }

  /** Créer un produit (SELLER / ADMIN) */
  createProduct(product: ProductRequest): Observable<ProductResponse> {
    return this.http.post<ProductResponse>(this.base, product).pipe(
      catchError(err => { console.error('Erreur création produit:', err); throw err; })
    );
  }

  /** Modifier un produit (SELLER / ADMIN) */
  updateProduct(id: number, product: Partial<ProductRequest>): Observable<ProductResponse> {
    return this.http.put<ProductResponse>(`${this.base}/${id}`, product).pipe(
      catchError(err => { console.error(`Erreur mise à jour produit ${id}:`, err); throw err; })
    );
  }

  /** Soft-delete d'un produit (SELLER / ADMIN) */
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`).pipe(
      catchError(err => { console.error(`Erreur suppression produit ${id}:`, err); throw err; })
    );
  }
}
