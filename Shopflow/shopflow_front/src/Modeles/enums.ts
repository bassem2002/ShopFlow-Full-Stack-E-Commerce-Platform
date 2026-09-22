// ============================================================
// Enums ShopFlow — synchronisés avec les enums Java backend
// Package Java : com.shopflow.shopflow.enums
// ============================================================

/** Rôles utilisateurs */
export type Role = 'ADMIN' | 'SELLER' | 'CUSTOMER';

/** Statuts d'une commande — suit le cycle de vie */
export type OrderStatus =
  | 'PENDING'
  | 'PAID'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED'
  | 'REFUNDED';

/** Type de réduction d'un coupon */
export type CouponType = 'PERCENT' | 'FIXED';

/** Labels FR pour l'affichage dans les composants */
export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING:    'En attente',
  PAID:       'Payée',
  PROCESSING: 'En traitement',
  SHIPPED:    'Expédiée',
  DELIVERED:  'Livrée',
  CANCELLED:  'Annulée',
  REFUNDED:   'Remboursée',
};

export const ROLE_LABELS: Record<Role, string> = {
  ADMIN:    'Administrateur',
  SELLER:   'Vendeur',
  CUSTOMER: 'Client',
};

export const COUPON_TYPE_LABELS: Record<CouponType, string> = {
  PERCENT: 'Pourcentage (%)',
  FIXED:   'Montant fixe (DT)',
};
