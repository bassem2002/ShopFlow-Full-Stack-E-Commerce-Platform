import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { inject } from '@angular/core';
import { AuthService } from '../../Services/auth.service';
import { Role } from '../../Modeles/enums';

import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, MatIconModule, MatButtonModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  hidePassword = true;
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  isAuthenticated = this.authService.isAuthenticated;
  private router = inject(Router);

  registerForm: FormGroup = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$/)]],
    role: ['CUSTOMER', Validators.required],
    storeName: [''],
    storeDescription: [''],
  });

  submitting = false;
  errorMessage: string | null = null;

  onSubmit(): void {
    if (this.registerForm.invalid) {
      if (this.registerForm.get('email')?.errors?.['email']) {
        this.errorMessage = "Format d'email invalide.";
      } else if (this.registerForm.get('password')?.errors) {
        this.errorMessage = "Le mot de passe ne respecte pas les critères (8 caractères, majuscule, chiffre).";
      } else if (this.registerForm.get('role')?.value === 'SELLER' && !this.registerForm.get('storeName')?.value) {
        this.errorMessage = "Le nom de la boutique est obligatoire pour les vendeurs.";
      } else {
        this.errorMessage = "Veuillez remplir tous les champs obligatoires.";
      }
      return;
    }

    this.submitting = true;
    this.errorMessage = null;

    this.authService.register(this.registerForm.value).subscribe({
      next: (response) => {
        this.submitting = false;
        const role = response.role;
        if (role === 'ADMIN') this.router.navigate(['/dashboard/admin']);
        else if (role === 'SELLER') this.router.navigate(['/dashboard/seller']);
        else this.router.navigate(['/dashboard/customer']);
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = err.error?.message || 'Erreur lors de l\'inscription.';
      }
    });
  }
}
