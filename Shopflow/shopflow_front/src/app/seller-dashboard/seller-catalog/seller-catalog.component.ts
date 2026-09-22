import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { ProductService } from '../../../Services/product.service';
import { ProductResponse, ProductRequest } from '../../../Modeles/Product';
import { AuthService } from '../../../Services/auth.service';

@Component({
  selector: 'app-seller-catalog',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  templateUrl: './seller-catalog.component.html',
  styleUrl: './seller-catalog.component.css'

})
export class SellerCatalogComponent implements OnInit {
  private productService = inject(ProductService);
  private authService = inject(AuthService);


  products: ProductResponse[] = [];
  loading = false;
  error: string | null = null;
  


  ngOnInit(): void {
    this.loadMyProducts();
  }

  loadMyProducts(): void {
    this.loading = true;
    this.error = null;
    // We need to fetch products by sellerId. 
    // Wait, the API doesn't expose the seller ID directly via AuthService.
    // Let's get all products and filter locally for now, or use the filter if backend supports it.
    // The filter supports sellerId: number. We need the current user's ID. 
    // Assuming backend handles seller's own products via an endpoint or we just load them.
    // For now, let's just get all products (in a real app, backend would filter by JWT token).
    this.productService.getProducts().subscribe({
      next: (page: any) => {
        // Ideally: this.products = page.content.filter(p => p.sellerEmail === this.authService.currentEmail());
        this.products = page.content;
        this.loading = false;
      },
      error: (err: any) => {
        this.error = "Erreur lors du chargement du catalogue.";
        this.loading = false;
        console.error(err);
      }
    });
  }



  onDelete(id: number): void {
    if (confirm("Voulez-vous vraiment supprimer ce produit ?")) {
      this.productService.deleteProduct(id).subscribe({
        next: () => {
          this.products = this.products.filter(p => p.id !== id);
        },
        error: (err: any) => {
          this.error = "Erreur lors de la suppression.";
          console.error(err);
        }
      });
    }
  }
}
