// ============================================================
// Modèle générique Pagination — Spring Page<T>
// Retourné par les endpoints paginés du backend
// ============================================================

export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;    // page courante (0-indexed)
  first: boolean;
  last: boolean;
}

// ─── Paramètres de filtre pour les requêtes paginées ─────────

export interface PageParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}
