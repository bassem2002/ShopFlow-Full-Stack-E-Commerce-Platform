import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../Services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  hidePassword = signal(true);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  isAuthenticated = this.authService.isAuthenticated;

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  togglePasswordVisibility() {
    this.hidePassword.update(val => !val);
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.isLoading.set(true);
      this.errorMessage.set(null);

      const email = this.loginForm.value.email as string;
      const password = this.loginForm.value.password as string;

      this.authService.login({ email, password }).subscribe({
        next: (response) => {
          this.isLoading.set(false);
          const returnUrl = this.route.snapshot.queryParams['returnUrl'];
          if (returnUrl) {
            this.router.navigateByUrl(returnUrl);
            return;
          }
          const role = this.authService.currentRole();
          if (role === 'ADMIN') this.router.navigate(['/dashboard/admin']);
          else if (role === 'SELLER') this.router.navigate(['/dashboard/seller']);
          else this.router.navigate(['/dashboard/customer']);
        },
        error: (err) => {
          this.isLoading.set(false);
          if (err.status === 401 || err.status === 403) {
            this.errorMessage.set('Email ou mot de passe incorrect.');
          } else {
            this.errorMessage.set('Erreur lors de la connexion.');
          }
        }
      });
    } else {
      this.loginForm.markAllAsTouched();
    }
  }
}