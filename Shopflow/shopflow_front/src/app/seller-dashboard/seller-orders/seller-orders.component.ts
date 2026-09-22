import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../../Services/order.service';
import { OrderResponse } from '../../../Modeles/Order';
import { ORDER_STATUS_LABELS, OrderStatus } from '../../../Modeles/enums';

@Component({
  selector: 'app-seller-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './seller-orders.component.html',
  styleUrl: './seller-orders.component.css'

})
export class SellerOrdersComponent implements OnInit {
  private orderService = inject(OrderService);
  
  orders: OrderResponse[] = [];
  loading = false;
  error: string | null = null;
  statusLabels = ORDER_STATUS_LABELS;

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.error = null;
    // Le vendeur voit les commandes contenant ses produits
    // On utilise getMyOrders() temporairement ou une méthode adaptée
    this.orderService.getMyOrders().subscribe({
      next: (data) => {
        this.orders = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = "Erreur lors du chargement de vos ventes.";
        this.loading = false;
        console.error(err);
      }
    });
  }

  getAvailableStatuses(currentStatus: OrderStatus): {key: string, label: string}[] {
    // Logique simplifiée : on propose tout sauf PENDING (qui est pour le client avant paiement)
    const all = Object.entries(this.statusLabels).map(([k, v]) => ({ key: k, label: v }));
    return all.filter(s => s.key !== 'PENDING');
  }

  updateStatus(order: OrderResponse, newStatus: string): void {
    this.orderService.updateOrderStatus(order.id, newStatus as OrderStatus).subscribe({
      next: (updatedOrder) => {
        order.status = updatedOrder.status;
      },
      error: (err) => {
        alert("Erreur lors de la mise à jour du statut");
        console.error(err);
      }
    });
  }
}
