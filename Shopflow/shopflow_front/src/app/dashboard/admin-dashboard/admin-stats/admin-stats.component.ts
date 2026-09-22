import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DashboardService } from '../../../../Services/dashboard.service';
import { AdminDashboard, OrderSummaryDto, TopProductDto, TopSellerDto } from '../../../../Modeles/Dashboard';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-admin-stats',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './admin-stats.component.html',
  styleUrl: './admin-stats.component.css'
})
export class AdminStatsComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  private destroyRef = inject(DestroyRef); 
  
  stats: AdminDashboard | null = null;
  loading = false;
  error: string | null = null;

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.loading = true;
    this.dashboardService.getAdminDashboard()
      .pipe(takeUntilDestroyed(this.destroyRef)) 
      .subscribe({
        next: (data) => {
          console.log('Stats reçues:', data);
          this.stats = data;
          this.loading = false;
        },
        error: (err) => {
          console.error('Erreur API Dashboard:', err);
          this.error = "Impossible de charger les statistiques. Vérifiez la connexion au serveur.";
          this.loading = false;
        }
      });
  }
}
