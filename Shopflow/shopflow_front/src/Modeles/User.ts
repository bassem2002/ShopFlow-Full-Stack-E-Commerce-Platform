// ============================================================
// Modèles User — synchronisés avec le backend ShopFlow
// Entité Java : User (implements UserDetails)
// ============================================================

import { Role } from './enums';

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
  active: boolean;
  createdAt: string;   // ISO 8601
}
