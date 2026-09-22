import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../../Services/auth.service';

// ============================================================
// Intercepteur HTTP — AuthInterceptor (Angular 19 Functional)
// Rôle :
//   1. Attacher le Bearer token JWT à chaque requête sortante
//   2. Sur 401 → tenter un refresh automatique, puis retry
//   3. Si refresh échoue → déconnecter et rediriger vers /login
// ============================================================

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router      = inject(Router);

  // Attacher le token si disponible
  const token = authService.getAccessToken();
  const clonedReq = token
    ? req.clone({ headers: req.headers.set('Authorization', `Bearer ${token}`) })
    : req;

  return next(clonedReq).pipe(
    catchError((error: HttpErrorResponse) => {

      // Erreur 401 : token expiré → tenter un refresh
      if (error.status === 401) {
        const refreshToken = authService.getRefreshToken();

        if (refreshToken) {
          return authService.refresh({ refreshToken }).pipe(
            switchMap(response => {
              // Retry la requête originale avec le nouveau token
              const retried = req.clone({
                headers: req.headers.set('Authorization', `Bearer ${response.accessToken}`)
              });
              return next(retried);
            }),
            catchError(refreshError => {
              // Refresh échoué → déconnexion forcée
              authService.clearSession();
              router.navigate(['/login']);
              return throwError(() => refreshError);
            })
          );
        }

        // Pas de refresh token → déconnecter
        authService.clearSession();
        router.navigate(['/login']);
      }

      // Erreur 403 : Accès refusé (Droits insuffisants)
      if (error.status === 403) {
        console.error('Accès refusé (403)');
        alert('🔒 Accès refusé : Vous n\'avez pas les permissions nécessaires pour effectuer cette action.');
        // Optionnel : router.navigate(['/forbidden']);
      }

      return throwError(() => error);
    })
  );
};
