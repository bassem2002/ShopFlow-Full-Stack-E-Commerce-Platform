import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../Services/dashboard.service';
import { CustomerDashboard } from '../../../Modeles/Dashboard';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './customer-dashboard.component.html',
  styleUrl: './customer-dashboard.component.css'
})
export class CustomerDashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  
  stats: CustomerDashboard | null = null;
  loading = false;
  error: string | null = null;

  ngOnInit(): void {
    this.loading = true;
    this.dashboardService.getCustomerDashboard().subscribe({
      next: (data) => {
        console.log('Données Dashboard Client reçues :', data);
        this.stats = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = "Impossible de charger vos statistiques.";
        this.loading = false;
        console.error(err);
      }
    });
  }
}
