import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UserService } from '../../../Services/user.service';
import { User } from '../../../Modeles/User';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  templateUrl: './user-list.component.html'
})
export class UserListComponent implements OnInit {
  private userService = inject(UserService);

  users: User[] = [];
  loading = false;
  error: string | null = null;

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = null;
    this.userService.getAllUsers().subscribe({
      next: (data) => { this.users = data; this.loading = false; },
      error: (err) => { this.error = 'Impossible de charger les utilisateurs.'; this.loading = false; console.error(err); }
    });
  }

  toggleStatus(user: User): void {
    const updatedStatus = !user.active;
    this.userService.updateUser(user.id, { active: updatedStatus }).subscribe({
      next: (res) => {
        user.active = res.active;
      },
      error: (err) => {
        this.error = "Impossible de mettre à jour le statut.";
        console.error(err);
      }
    });
  }
}
