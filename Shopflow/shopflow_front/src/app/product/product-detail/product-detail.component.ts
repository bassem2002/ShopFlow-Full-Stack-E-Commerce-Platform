import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ProductService } from '../../../Services/product.service';
import { ProductResponse } from '../../../Modeles/Product';
import { CartService } from '../../../Services/cart.service';
import { AuthService } from '../../../Services/auth.service';
import { ReviewService } from '../../../Services/review.service';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule, MatIconModule],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css'
})
export class ProductDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private productService = inject(ProductService);
  private cartService = inject(CartService);
  private authService = inject(AuthService);
  private reviewService = inject(ReviewService);
  private fb = inject(FormBuilder);

  product: ProductResponse | null = null;
  loading = true;
  error: string | null = null;
  isAdminOrSeller = false;
  isCustomer = false;
  selectedVariant: any | null = null;

  reviewForm = this.fb.group({
    rating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
    comment: ['', [Validators.maxLength(1000)]]
  });

  submittingReview = false;
  reviewError: string | null = null;

  ngOnInit(): void {
    this.isAdminOrSeller = this.authService.isAdmin() || this.authService.isSeller();
    this.isCustomer = this.authService.isCustomer();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadProduct(+id);
    }
  }

  loadProduct(id: number): void {
    this.productService.getProductById(id).subscribe({
      next: (data) => {
        this.product = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = "Impossible de charger les détails du produit.";
        this.loading = false;
      }
    });
  }

  selectVariant(variant: any): void {
    this.selectedVariant = variant;
  }

  addToCart(): void {
    if (this.isAdminOrSeller) {
      alert("En tant qu'administrateur ou vendeur, vous ne pouvez pas ajouter d'articles au panier.");
      return;
    }

    if (!this.product) return;
    const customerId = this.authService.getUserId();
    
    if (!customerId) {
      alert("Veuillez vous connecter pour ajouter des articles au panier.");
      return;
    }

    if (this.product.variants && this.product.variants.length > 0 && !this.selectedVariant) {
      alert("Veuillez sélectionner une variante (Taille, Couleur, etc.) avant l'ajout.");
      return;
    }

    this.cartService.addItem({
      customerId: customerId,
      productId: this.product.id,
      variantId: this.selectedVariant?.id || null,
      quantity: 1
    }).subscribe({
      next: () => alert(`Produit ${this.product?.name} ajouté au panier !`),
      error: (err) => alert("Erreur lors de l'ajout au panier.")
    });
  }

  submitReview(): void {
    if (this.reviewForm.invalid || !this.product) return;
    
    const userId = this.authService.getUserId();
    if (!userId) {
      alert("Veuillez vous connecter pour laisser un avis.");
      return;
    }

    this.submittingReview = true;
    this.reviewError = null;

    const request = {
      productId: this.product.id,
      rating: this.reviewForm.value.rating ?? 5,
      comment: this.reviewForm.value.comment ?? ''
    };

    this.reviewService.createReview(userId, request).subscribe({
      next: (newReview) => {
        this.submittingReview = false;
        if (this.product) {
          this.product.reviews = [newReview, ...this.product.reviews];
          // Recalculer la moyenne locale
          const totalRating = this.product.reviews.reduce((sum, r) => sum + r.rating, 0);
          this.product.averageRating = totalRating / this.product.reviews.length;
        }
        this.reviewForm.reset({ rating: 5, comment: '' });
        alert("Merci pour votre avis !");
      },
      error: (err) => {
        this.submittingReview = false;
        this.reviewError = err.error?.message || "Erreur lors de l'envoi de l'avis. Vérifiez que vous avez bien reçu ce produit.";
      }
    });
  }
}
