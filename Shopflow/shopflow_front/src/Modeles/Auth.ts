// ============================================================
// Modèles Auth — synchronisés avec les DTOs Java backend
// AuthController : /api/auth
// ============================================================

import { Role } from './enums';

// ─── Request DTOs (Angular → API) ────────────────────────────

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: Role;               // CUSTOMER ou SELLER
  // Champs optionnels pour SELLER
  storeName?: string;
  storeDescription?: string;
  storeLogo?: string;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface LogoutRequest {
  refreshToken: string;
}

// ─── Response DTOs (API → Angular) ───────────────────────────

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  email: string;
  role: Role;
  userId: number;
}

// ─── Modèle utilisateur stocké localement ────────────────────

export interface LocalUser {
  email: string;
  role: Role;
}
