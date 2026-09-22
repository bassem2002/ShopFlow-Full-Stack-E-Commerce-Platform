// ============================================================
// Modèles Category — synchronisés avec le backend ShopFlow
// CategoryController : /api/categories
// ============================================================

// ─── Request DTO ─────────────────────────────────────────────

export interface CategoryRequest {
  name: string;
  description?: string;
  imageUrl?: string;
  parentId?: number | null;
}

// ─── Response DTO ────────────────────────────────────────────

export interface CategoryResponse {
  id: number;
  name: string;
  description: string | null;
  imageUrl: string | null;
  parentId: number | null;
  children: CategoryResponse[];   // récursif pour l'arbre hiérarchique
}
