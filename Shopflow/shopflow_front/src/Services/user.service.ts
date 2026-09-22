import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';
import { environment } from '../environments/environment';
import { User } from '../Modeles/User';


// ============================================================
// Service : UserService
// Description : Gestion des opérations CRUD pour les utilisateurs/clients
// Équivalent FirstApp : member.service.ts
// URL Backend : ${apiUrl}/users
// ============================================================

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/users`;

  /**
   * Récupère la liste de tous les utilisateurs
   */
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl).pipe(
      catchError((error) => {
        console.error('Erreur lors du chargement des utilisateurs :', error);
        throw error;
      })
    );
  }

  /**
   * Récupère un utilisateur par son ID
   */
  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`).pipe(
      catchError((error) => {
        console.error(`Erreur lors du chargement de l'utilisateur ${id} :`, error);
        throw error;
      })
    );
  }

  /**
   * Crée un nouvel utilisateur
   */
  createUser(user: Omit<User, 'id'>): Observable<User> {
    return this.http.post<User>(this.apiUrl, user).pipe(
      catchError((error) => {
        console.error('Erreur lors de la création de l\'utilisateur :', error);
        throw error;
      })
    );
  }

  /**
   * Met à jour un utilisateur existant
   */
  updateUser(id: number, user: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, user).pipe(
      catchError((error) => {
        console.error(`Erreur lors de la mise à jour de l'utilisateur ${id} :`, error);
        throw error;
      })
    );
  }

  /**
   * Supprime un utilisateur
   */
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError((error) => {
        console.error(`Erreur lors de la suppression de l'utilisateur ${id} :`, error);
        throw error;
      })
    );
  }
}
