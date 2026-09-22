import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { ProductService } from '../../Services/product.service';
import { AuthService } from '../../Services/auth.service';
import { ProductResponse } from '../../Modeles/Product';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { CategoryService } from '../../Services/category.service';
import { CategoryResponse } from '../../Modeles/Category';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule, MatButtonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  private productService = inject(ProductService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private categoryService = inject(CategoryService);

  featuredProducts = signal<ProductResponse[]>([]);
  categories = signal<CategoryResponse[]>([]);
  loading = signal(false);
  isAdminOrSeller = signal(false);
  isGuest = signal(true);

  ngOnInit(): void {
    this.isAdminOrSeller.set(this.authService.isAdmin() || this.authService.isSeller());
    this.isGuest.set(!this.authService.isLoggedIn());
    this.loadFeaturedProducts();
    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (data) => this.categories.set(data.slice(0, 8)), // On en garde 8 pour la home
      error: (err) => console.error('Erreur categories home', err)
    });
  }

  loadFeaturedProducts(): void {
    this.loading.set(true);
    // On récupère les 4 premiers produits pour la home
    this.productService.getProducts({ size: 4, sortBy: 'createdAt', sortDirection: 'desc' }).subscribe({
      next: (page) => {
        this.featuredProducts.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onProductClick(productId: number): void {
    this.router.navigate(['/products', productId]);
  }

  onCategoryClick(categoryId: number): void {
    this.router.navigate(['/products'], { queryParams: { categoryId: categoryId } });
  }
}
