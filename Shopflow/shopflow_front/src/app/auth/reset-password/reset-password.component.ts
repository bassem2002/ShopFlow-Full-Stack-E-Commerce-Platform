import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../Services/auth.service';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MatIconModule],
  template: `
    <div class="min-h-screen bg-slate-50 flex items-center justify-center p-6">
      <div class="bg-white w-full max-w-md rounded-[40px] shadow-2xl p-10 border border-slate-100 animate-fade-in">
        <div class="text-center mb-10">
          <div class="w-20 h-20 bg-emerald-50 text-emerald-600 rounded-3xl flex items-center justify-center mx-auto mb-6 shadow-inner">
            <mat-icon class="!text-4xl w-10 h-10">lock_open</mat-icon>
          </div>
          <h1 class="text-3xl font-heading font-black text-slate-900 tracking-tight mb-2">Nouveau mot de passe</h1>
          <p class="text-slate-400 font-medium text-sm px-6">Sécurisez votre compte avec un nouveau mot de passe fort.</p>
        </div>

        <div *ngIf="error" class="bg-rose-50 border border-rose-100 text-rose-600 p-4 rounded-2xl mb-8 flex items-center gap-3 animate-shake">
          <mat-icon>error_outline</mat-icon>
          <span class="text-xs font-bold">{{ error }}</span>
        </div>

        <form (ngSubmit)="onSubmit()" #resetForm="ngForm" class="space-y-6">
          <div class="space-y-2">
            <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-4">Nouveau Mot de Passe</label>
            <div class="relative">
              <input type="password" name="password" [(ngModel)]="password" required minlength="8"
                class="w-full bg-slate-50 border border-slate-100 rounded-2xl px-6 py-4 text-sm font-medium focus:ring-2 focus:ring-indigo-100 focus:bg-white outline-none transition-all pl-12"
                placeholder="••••••••">
              <mat-icon class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-300">key</mat-icon>
            </div>
            <p class="text-[9px] text-slate-400 font-bold uppercase tracking-tighter px-4 mt-1">Min. 8 caractères, 1 chiffre, 1 majuscule</p>
          </div>

          <button type="submit" [disabled]="loading || resetForm.invalid"
            class="w-full bg-indigo-600 text-white rounded-2xl py-4 font-black shadow-xl shadow-indigo-100 hover:scale-[1.02] active:scale-[0.98] transition-all disabled:opacity-50 flex items-center justify-center gap-3">
            <span *ngIf="!loading">Réinitialiser</span>
            <span *ngIf="loading" class="animate-spin"><mat-icon>autorenew</mat-icon></span>
          </button>
        </form>
      </div>
    </div>
  `
})
export class ResetPasswordComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  private router = inject(Router);

  token = '';
  password = '';
  loading = false;
  error = '';

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'];
    if (!this.token) {
      this.error = "Token de réinitialisation manquant.";
    }
  }

  onSubmit(): void {
    this.loading = true;
    this.error = '';

    this.authService.resetPassword(this.token, this.password).subscribe({
      next: () => {
        alert("Mot de passe mis à jour avec succès !");
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || "Lien invalide ou expiré.";
      }
    });
  }
}
