import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OrderResponse } from '../../Modeles/Order';
import { ORDER_STATUS_LABELS, OrderStatus } from '../../Modeles/enums';
import { OrderService } from '../../Services/order.service';
import { AuthService } from '../../Services/auth.service';

// ============================================================
// Composant : OrderComponent
// Description : Affichage et gestion des commandes ShopFlow
// Modèle utilisé : Order (src/Modeles/Order.ts)
// Route : /orders
// ============================================================

import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-order',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  templateUrl: './order.component.html',
  styleUrls: ['./order.component.css'],
})
export class OrderComponent implements OnInit {
  private orderService = inject(OrderService);
  public authService = inject(AuthService);

  orders: OrderResponse[] = [];
  loading = false;
  error: string | null = null;
  selectedOrder: OrderResponse | null = null;
  updatingStatus = false;

  readonly statusLabels = ORDER_STATUS_LABELS;
  readonly allStatuses: OrderStatus[] = [
    'PENDING', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED'
  ];

  ngOnInit(): void {
    this.loadOrders();
  }

  updateStatus(order: OrderResponse, newStatus: OrderStatus): void {
    if (order.status === newStatus) return;
    
    this.updatingStatus = true;
    this.orderService.updateOrderStatus(order.id, newStatus).subscribe({
      next: (updatedOrder) => {
        // Mettre à jour l'objet local dans la liste
        const idx = this.orders.findIndex(o => o.id === updatedOrder.id);
        if (idx > -1) this.orders[idx] = updatedOrder;
        
        // Si c'est l'ordre ouvert dans la modal, le mettre à jour aussi
        if (this.selectedOrder?.id === updatedOrder.id) {
          this.selectedOrder = updatedOrder;
        }
        
        this.updatingStatus = false;
      },
      error: (err) => {
        this.error = "Erreur lors de la mise à jour du statut.";
        this.updatingStatus = false;
        console.error(err);
      }
    });
  }

  cancelOrder(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir annuler cette commande ?')) {
      this.orderService.cancelOrder(id).subscribe({
        next: (updatedOrder) => {
          const idx = this.orders.findIndex(o => o.id === updatedOrder.id);
          if (idx > -1) this.orders[idx] = updatedOrder;
          alert("Commande annulée avec succès.");
        },
        error: (err) => {
          alert("Erreur lors de l'annulation de la commande : " + (err.error?.message || "Inconnue"));
        }
      });
    }
  }

  loadOrders(): void {
    this.loading = true;
    this.error = null;

    if (this.authService.isAdmin()) {
      this.orderService.getAllOrders().subscribe({
        next: (data) => {
          this.orders = data;
          this.loading = false;
        },
        error: (err) => {
          this.error = "Erreur lors du chargement de toutes les commandes.";
          this.loading = false;
        }
      });
    } else if (this.authService.isCustomer() || this.authService.isSeller()) {
      this.orderService.getMyOrders().subscribe({
        next: (data) => {
          this.orders = data;
          this.loading = false;
        },
        error: (err) => {
          this.error = "Erreur lors du chargement de vos commandes.";
          this.loading = false;
        }
      });
    } else {
      this.loading = false;
    }
  }

  viewDetail(order: OrderResponse): void {
    this.selectedOrder = order;
  }

  closeDetail(): void {
    this.selectedOrder = null;
  }

  getStatusClass(status: OrderStatus): string {
    const map: Record<OrderStatus, string> = {
      PENDING:    'status-pending',
      PAID:       'status-paid',
      PROCESSING: 'status-processing',
      SHIPPED:    'status-shipped',
      DELIVERED:  'status-delivered',
      CANCELLED:  'status-cancelled',
      REFUNDED:   'status-refunded',
    };
    return map[status];
  }

  getTotalItems(order: OrderResponse): number {
    return order.items.reduce((sum: number, item: { quantity: number }) => sum + item.quantity, 0);
  }
}
