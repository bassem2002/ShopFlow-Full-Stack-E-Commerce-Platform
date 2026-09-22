import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SellerService, SellerStats } from '../../../Services/seller.service';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-seller-stats',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  templateUrl: './seller-stats.component.html',
  styleUrl: './seller-stats.component.css'

})
export class SellerStatsComponent implements OnInit {
  private sellerService = inject(SellerService);
  
  stats = signal<SellerStats | null>(null);
  loading = signal<boolean>(false);
  error = signal<string | null>(null);
  today = new Date();

  ngOnInit(): void {
    this.loading.set(true);
    this.sellerService.getDashboardStats().subscribe({
      next: (data: SellerStats) => {
        console.log('Stats reçues:', data);
        this.stats.set(data);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        console.error('Erreur lors du chargement des stats:', err);
        // Fournir des données par défaut pour le développement
        this.stats.set({
          totalRevenue: 0,
          totalProducts: 0,
          pendingOrders: 0,
          lowStockAlerts: [],
          recentOrders: []
        });
        this.loading.set(false);
      }
    });
  }
}

