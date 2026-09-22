import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../Services/auth.service';
import { UserService } from '../../../Services/user.service';
import { User } from '../../../Modeles/User';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatIconModule, MatButtonModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private userService = inject(UserService);

  user = signal<User | null>(null);
  loading = signal(false);
  saving = signal(false);
  error = signal<string | null>(null);
  success = signal(false);

  profileForm = this.fb.group({
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]]
  });

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    const userId = this.authService.getUserId();
    if (!userId) {
      this.error.set("Session expirée. Veuillez vous reconnecter.");
      return;
    }

    this.loading.set(true);
    this.userService.getUserById(userId).subscribe({
      next: (userData: User) => {
        this.user.set(userData);
        this.profileForm.patchValue({
          firstName: userData.firstName,
          lastName: userData.lastName
        });
        this.loading.set(false);
      },
      error: (err: any) => {
        console.error(err);
        this.error.set("Impossible de charger les données du profil.");
        this.loading.set(false);
      }
    });
  }

  onSubmit(): void {
    if (this.profileForm.invalid || !this.user()) return;

    this.saving.set(true);
    this.error.set(null);
    this.success.set(false);

    const userId = this.user()!.id;
    const updateData = this.profileForm.value as Partial<User>;

    this.userService.updateUser(userId, updateData).subscribe({
      next: (updatedUser: User) => {
        this.user.set(updatedUser);
        this.saving.set(false);
        this.success.set(true);
        
        // Cacher le message de succès après 5 secondes
        setTimeout(() => this.success.set(false), 5000);
      },
      error: (err: any) => {
        console.error(err);
        this.error.set("Une erreur est survenue lors de la mise à jour.");
        this.saving.set(false);
      }
    });
  }
}
