import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../environments/environment';
import { AuthResponse, LoginRequest, LogoutRequest, RefreshRequest, RegisterRequest } from '../Modeles/Auth';

// ============================================================
// Service : AuthService
// Description : Authentification JWT — login, register, refresh, logout
// Endpoint : /api/auth
// Stockage : localStorage (accessToken, refreshToken, role, email)
// ============================================================

import { Auth, signInWithEmailAndPassword, createUserWithEmailAndPassword, signOut, idToken, signInWithPopup, GoogleAuthProvider } from '@angular/fire/auth';
import { from, switchMap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private firebaseAuth = inject(Auth);
  private base = `${environment.apiUrl}/auth`;

  /** Connexion via Google (Popup) */
  loginWithGoogle(role: string = 'CUSTOMER'): Observable<AuthResponse> {
    const provider = new GoogleAuthProvider();
    return from(signInWithPopup(this.firebaseAuth, provider)).pipe(
      switchMap(userCredential => from(userCredential.user.getIdToken())),
      switchMap(token => this.http.post<AuthResponse>(`${this.base}/firebase`, { idToken: token, role: role })),
      tap(response => this.storeSession(response))
    );
  }

  /** Inscription via Firebase (Email/Password) */
  register(request: RegisterRequest): Observable<AuthResponse> {
    return from(createUserWithEmailAndPassword(this.firebaseAuth, request.email, request.password)).pipe(
      switchMap(userCredential => from(userCredential.user.getIdToken())),
      switchMap(token => this.http.post<AuthResponse>(`${this.base}/firebase`, { idToken: token, role: request.role })),
      tap(response => this.storeSession(response))
    );
  }

  /** Connexion via Firebase (Email/Password) */
  login(request: LoginRequest): Observable<AuthResponse> {
    return from(signInWithEmailAndPassword(this.firebaseAuth, request.email, request.password)).pipe(
      switchMap(userCredential => from(userCredential.user.getIdToken())),
      switchMap(token => this.http.post<AuthResponse>(`${this.base}/firebase`, { idToken: token })),
      tap(response => this.storeSession(response))
    );
  }

  // ─── Clés localStorage ───────────────────────────────────
  private readonly TOKEN_KEY   = 'accessToken';
  private readonly REFRESH_KEY = 'refreshToken';
  private readonly ROLE_KEY    = 'role';
  private readonly EMAIL_KEY   = 'email';
  private readonly USER_ID_KEY = 'userId';

  // ─── Signals pour UI Réactive ─────────────────────────────
  private roleSignal = signal<string | null>(this.getRole());
  private emailSignal = signal<string | null>(this.getEmail());
  private userIdSignal = signal<number | null>(this.getUserId());

  public currentRole = this.roleSignal.asReadonly();
  public currentEmail = this.emailSignal.asReadonly();
  public currentUserId = this.userIdSignal.asReadonly();
  public isAuthenticated = computed(() => !!this.roleSignal());

  /** Renouvellement du accessToken via le refreshToken */
  refresh(request: RefreshRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/refresh`, request).pipe(
      tap(response => this.storeSession(response))
    );
  }

  /** Déconnexion — révoque le refreshToken en base */
  logout(): Observable<void> {
    const refreshToken = this.getRefreshToken();
    const req: LogoutRequest = { refreshToken: refreshToken ?? '' };
    return this.http.post<void>(`${this.base}/logout`, req).pipe(
      tap(() => this.clearSession())
    );
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(`${this.base}/forgot-password?email=${email}`, {});
  }

  resetPassword(token: string, password: string): Observable<void> {
    return this.http.post<void>(`${this.base}/reset-password?token=${token}&newPassword=${password}`, {});
  }

  // ─── Gestion localStorage ─────────────────────────────────

  private storeSession(response: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY,   response.accessToken);
    localStorage.setItem(this.REFRESH_KEY, response.refreshToken);
    localStorage.setItem(this.ROLE_KEY,    response.role);
    localStorage.setItem(this.EMAIL_KEY,   response.email);
    localStorage.setItem(this.USER_ID_KEY, response.userId.toString());
    this.roleSignal.set(response.role);
    this.emailSignal.set(response.email);
    this.userIdSignal.set(response.userId);
  }

  clearSession(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
    localStorage.removeItem(this.ROLE_KEY);
    localStorage.removeItem(this.EMAIL_KEY);
    localStorage.removeItem(this.USER_ID_KEY);
    this.roleSignal.set(null);
    this.emailSignal.set(null);
    this.userIdSignal.set(null);
  }

  // ─── Getters ──────────────────────────────────────────────

  getAccessToken(): string | null  { return localStorage.getItem(this.TOKEN_KEY); }
  getRefreshToken(): string | null { return localStorage.getItem(this.REFRESH_KEY); }
  getRole(): string | null         { return localStorage.getItem(this.ROLE_KEY); }
  getEmail(): string | null        { return localStorage.getItem(this.EMAIL_KEY); }
  getUserId(): number | null {
    const id = localStorage.getItem(this.USER_ID_KEY);
    return id ? parseInt(id, 10) : null;
  }

  isLoggedIn(): boolean { return !!this.getAccessToken(); }

  hasRole(role: string): boolean { return this.getRole() === role; }
  isAdmin():    boolean { return this.hasRole('ADMIN'); }
  isSeller():   boolean { return this.hasRole('SELLER'); }
  isCustomer(): boolean { return this.hasRole('CUSTOMER'); }
}
