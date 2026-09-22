import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../Services/dashboard.service';
import { CustomerDashboard } from '../../Modeles/Dashboard';

import { ORDER_STATUS_LABELS } from '../../Modeles/enums';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './customer-dashboard.component.html',
  styleUrl: './customer-dashboard.component.css'
})
export class CustomerDashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  
  stats: CustomerDashboard | null = null;
  loading = false;
  error: string | null = null;
  statusLabels = ORDER_STATUS_LABELS;

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.dashboardService.getCustomerDashboard().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = "Impossible de charger votre compte.";
        this.loading = false;
        console.error(err);
      }
    });
  }
}
