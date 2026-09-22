import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../Services/auth.service';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MatIconModule],
  template: `
    <div class="min-h-screen bg-slate-50 flex items-center justify-center p-6">
      <div class="bg-white w-full max-w-md rounded-[40px] shadow-2xl p-10 border border-slate-100 animate-fade-in">
        <div class="text-center mb-10">
          <div class="w-20 h-20 bg-indigo-50 text-indigo-600 rounded-3xl flex items-center justify-center mx-auto mb-6 shadow-inner">
            <mat-icon class="!text-4xl w-10 h-10">lock_reset</mat-icon>
          </div>
          <h1 class="text-3xl font-heading font-black text-slate-900 tracking-tight mb-2">Mot de passe oublié ?</h1>
          <p class="text-slate-400 font-medium text-sm px-6">Entrez votre email pour recevoir les instructions de réinitialisation.</p>
        </div>

        <div *ngIf="message" class="bg-emerald-50 border border-emerald-100 text-emerald-600 p-4 rounded-2xl mb-8 flex items-center gap-3 animate-slide-up">
          <mat-icon>check_circle</mat-icon>
          <span class="text-xs font-bold">{{ message }}</span>
        </div>

        <div *ngIf="error" class="bg-rose-50 border border-rose-100 text-rose-600 p-4 rounded-2xl mb-8 flex items-center gap-3 animate-shake">
          <mat-icon>error_outline</mat-icon>
          <span class="text-xs font-bold">{{ error }}</span>
        </div>

        <form (ngSubmit)="onSubmit()" #forgotForm="ngForm" class="space-y-6">
          <div class="space-y-2">
            <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-4">Email Address</label>
            <div class="relative">
              <input type="email" name="email" [(ngModel)]="email" required email
                class="w-full bg-slate-50 border border-slate-100 rounded-2xl px-6 py-4 text-sm font-medium focus:ring-2 focus:ring-indigo-100 focus:bg-white outline-none transition-all pl-12"
                placeholder="votre@email.com">
              <mat-icon class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-300">mail_outline</mat-icon>
            </div>
          </div>

          <button type="submit" [disabled]="loading || forgotForm.invalid"
            class="w-full bg-indigo-600 text-white rounded-2xl py-4 font-black shadow-xl shadow-indigo-100 hover:scale-[1.02] active:scale-[0.98] transition-all disabled:opacity-50 flex items-center justify-center gap-3">
            <span *ngIf="!loading">Envoyer le lien</span>
            <span *ngIf="loading" class="animate-spin"><mat-icon>autorenew</mat-icon></span>
          </button>

          <div class="text-center">
            <a routerLink="/login" class="text-[10px] font-black text-slate-400 uppercase tracking-widest hover:text-indigo-600 transition-colors flex items-center justify-center gap-2">
              <mat-icon class="!text-sm">arrow_back</mat-icon> Retour à la connexion
            </a>
          </div>
        </form>
      </div>
    </div>
  `
})
export class ForgotPasswordComponent {
  private authService = inject(AuthService);
  email = '';
  loading = false;
  message = '';
  error = '';

  onSubmit(): void {
    this.loading = true;
    this.message = '';
    this.error = '';

    this.authService.forgotPassword(this.email).subscribe({
      next: () => {
        this.loading = false;
        this.message = "Si cet email existe, un lien de réinitialisation vous a été envoyé.";
      },
      error: (err) => {
        this.loading = false;
        this.error = "Une erreur est survenue. Veuillez réessayer.";
      }
    });
  }
}
