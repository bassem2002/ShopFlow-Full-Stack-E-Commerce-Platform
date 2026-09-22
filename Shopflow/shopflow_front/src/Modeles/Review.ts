// ============================================================
// Modèles Review — synchronisés avec le backend ShopFlow
// ReviewController : /api/reviews
// ============================================================

// ─── Request DTO ─────────────────────────────────────────────

export interface ReviewRequest {
  productId: number;
  rating: number;       // 1 à 5
  comment?: string;
}

// ─── Response DTO ────────────────────────────────────────────

export interface ReviewResponse {
  id: number;
  productId: number;
  productName: string;
  userId: number;
  userName: string;
  rating: number;
  comment: string | null;
  createdAt: string;    // ISO 8601
}
