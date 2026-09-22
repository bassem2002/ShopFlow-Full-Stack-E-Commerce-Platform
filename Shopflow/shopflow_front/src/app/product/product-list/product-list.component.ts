import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ProductService } from '../../../Services/product.service';
import { ProductResponse, ProductFilter } from '../../../Modeles/Product';
import { CategoryService } from '../../../Services/category.service';
import { CategoryResponse } from '../../../Modeles/Category';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../Services/auth.service';
import { CartService } from '../../../Services/cart.service';

// ============================================================
// Composant : ProductListComponent
// Description : Affiche la liste de tous les produits
// Équivalent FirstApp : ArticleListComponent
// Route : /product
// ============================================================

import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, MatIconModule],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})

export class ProductListComponent implements OnInit {
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private authService = inject(AuthService);
  private cartService = inject(CartService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  products: ProductResponse[] = [];
  categories: CategoryResponse[] = [];
  loading = false;
  error: string | null = null;
  
  searchQuery = '';
  selectedCategoryId: number | null = null;
  maxPrice: number = 5000;
  onlyPromo = false;
  
  isAdminOrSeller = false;

  ngOnInit(): void {
    this.isAdminOrSeller = this.authService.isAdmin() || this.authService.isSeller();
    this.loadCategories();
    
    // Écouter les paramètres de l'URL pour filtrer par catégorie ou recherche
    this.route.queryParams.subscribe(params => {
      const catId = params['categoryId'];
      const search = params['search'];
      
      if (search) {
        this.searchQuery = search;
        this.onSearch();
      } else if (catId) {
        this.selectedCategoryId = Number(catId);
        this.onFilter();
      } else {
        this.loadProducts();
      }
    });
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (data) => this.categories = data,
      error: (err) => console.error("Erreur catégories", err)
    });
  }

  loadProducts(filters?: ProductFilter): void {
    this.loading = true;
    this.error = null;
    this.productService.getProducts(filters).subscribe({
      next: (page) => { this.products = page.content; this.loading = false; },
      error: (err: unknown) => { this.error = 'Impossible de charger les produits.'; this.loading = false; console.error(err); }
    });
  }
  
  onSearch(): void {
    if (this.searchQuery.trim() !== '') {
      this.loading = true;
      this.productService.searchProducts(this.searchQuery).subscribe({
        next: (page) => { this.products = page.content; this.loading = false; },
        error: (err) => { this.error = "Erreur de recherche"; this.loading = false; }
      });
    } else {
      this.onFilter();
    }
  }

  onFilter(): void {
    const filters: ProductFilter = {
      minPrice: 0,
      maxPrice: this.maxPrice,
      promo: this.onlyPromo || undefined
    };
    if (this.selectedCategoryId) {
      filters.categoryId = this.selectedCategoryId;
    }
    this.loadProducts(filters);
  }

  addToCart(product: ProductResponse): void {
    if (this.isAdminOrSeller) {
      alert("En tant qu'administrateur ou vendeur, vous ne pouvez pas ajouter d'articles au panier.");
      return;
    }

    const customerId = this.authService.getUserId();
    
    if (!customerId) {
      alert("Veuillez vous connecter pour ajouter des articles au panier.");
      return;
    }

    this.cartService.addItem({
      customerId: customerId,
      productId: product.id,
      quantity: 1
    }).subscribe({
      next: () => alert(`Produit ${product.name} ajouté au panier !`),
      error: (err) => alert("Erreur lors de l'ajout au panier.")
    });
  }

  viewProduct(id: number): void {
    this.router.navigate(['/products', id]);
  }

  onDelete(id: number): void {
    if (confirm('Supprimer ce produit ?')) {
      this.productService.deleteProduct(id).subscribe({
        next: () => { this.products = this.products.filter((p: ProductResponse) => p.id !== id); },
        error: (err: unknown) => { this.error = 'Impossible de supprimer le produit.'; console.error(err); }
      });
    }
  }
}
