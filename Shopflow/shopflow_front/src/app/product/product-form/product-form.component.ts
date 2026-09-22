import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  FormArray,
  Validators,
  AbstractControl,
  ValidationErrors
} from '@angular/forms';

function minArrayLengthValidator(min: number) {
  return (c: AbstractControl): ValidationErrors | null => {
    if (c.value && Array.isArray(c.value) && c.value.length >= min) {
      return null;
    }
    return { minLengthArray: { valid: false } };
  };
}
import { ProductService } from '../../../Services/product.service';
import { CategoryService } from '../../../Services/category.service';
import { CategoryResponse } from '../../../Modeles/Category';
import { AuthService } from '../../../Services/auth.service';

import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, MatIconModule],
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.css'
})

export class ProductFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  productForm!: FormGroup;
  categories: CategoryResponse[] = [];
  submitting = false;
  isEditMode = false;
  productId: number | null = null;
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.initForm();
    this.loadCategories();
    
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.productId = +id;
      this.loadProduct(this.productId);
    }
  }

  initForm(): void {
    this.productForm = this.fb.group({
      name: ['', Validators.required],
      description: ['', Validators.required],
      price: [0, [Validators.required, Validators.min(0)]],
      promoPrice: [null],
      stock: [0, [Validators.required, Validators.min(0)]],
      categoryIds: this.fb.array([], [minArrayLengthValidator(1)]),
      images: this.fb.array([this.fb.control('')]),
      variants: this.fb.array([])
    });
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe(data => this.categories = data);
  }

  loadProduct(id: number): void {
    this.productService.getProductById(id).subscribe({
      next: (product) => {
        // Remplir les images
        this.images.clear();
        product.images.forEach(img => this.addImage(img));
        if (this.images.length === 0) this.addImage();

        // Remplir les variantes
        this.variants.clear();
        product.variants.forEach(v => this.addVariant(v));

        this.productForm.patchValue({
          name: product.name,
          description: product.description,
          price: product.price,
          promoPrice: product.promoPrice,
          stock: product.stock
        });

        const categoryIdsArray = this.productForm.get('categoryIds') as FormArray;
        categoryIdsArray.clear();
        if (product.categoryIds) {
          product.categoryIds.forEach((catId: number) => categoryIdsArray.push(this.fb.control(catId)));
        }
      }
    });
  }

  get images() { return this.productForm.get('images') as FormArray; }
  get variants() { return this.productForm.get('variants') as FormArray; }

  addImage(url: string = ''): void {
    this.images.push(this.fb.control(url));
  }

  removeImage(index: number): void {
    this.images.removeAt(index);
  }

  addVariant(variant?: any): void {
    this.variants.push(this.fb.group({
      attribute: [variant?.attribute || '', Validators.required],
      value: [variant?.value || '', Validators.required],
      stockAdditional: [variant?.stockAdditional || 0],
      priceDelta: [variant?.priceDelta || 0]
    }));
  }

  removeVariant(index: number): void {
    this.variants.removeAt(index);
  }

  onCategoryChange(event: any, catId: number): void {
    const categoryIdsArray = this.productForm.get('categoryIds') as FormArray;
    if (event.target.checked) {
      categoryIdsArray.push(this.fb.control(catId));
    } else {
      const index = categoryIdsArray.controls.findIndex(ctrl => ctrl.value === catId);
      if (index > -1) categoryIdsArray.removeAt(index);
    }
    categoryIdsArray.markAsDirty();
    categoryIdsArray.markAsTouched();
  }

  isCategorySelected(catId: number): boolean {
    const categoryIdsArray = this.productForm?.get('categoryIds') as FormArray;
    if (!categoryIdsArray || !categoryIdsArray.controls) return false;
    return categoryIdsArray.controls.some(ctrl => ctrl.value === catId);
  }

  onSubmit(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    const categoryIds = this.productForm.get('categoryIds')?.value;
    if (!categoryIds || categoryIds.length === 0) {
      this.errorMessage = "Veuillez sélectionner au moins une catégorie.";
      return;
    }

    this.submitting = true;
    this.errorMessage = null;

    const sellerId = this.authService.getUserId();
    if (!sellerId) {
      this.errorMessage = "Vous devez être connecté.";
      this.submitting = false;
      return;
    }

    const formValue = this.productForm.value;
    const productData = {
      ...formValue,
      sellerId: sellerId,
      name: formValue.name?.trim(),
      description: formValue.description?.trim(),
      price: Number(formValue.price),
      promoPrice: formValue.promoPrice != null && formValue.promoPrice !== '' ? Number(formValue.promoPrice) : null,
      categoryIds: formValue.categoryIds ? formValue.categoryIds.map(Number) : [],
      stock: Number(formValue.stock),
      // Nettoyer les images vides
      images: formValue.images.map((img: string) => img.trim()).filter((img: string) => img !== ''),
      // Nettoyer et caster les variantes
      variants: formValue.variants
        .filter((v: any) => v.attribute?.trim() && v.value?.trim()) // Ne garde que les variantes où Attribut ET Valeur sont remplis
        .map((v: any) => ({
          attribute: v.attribute?.trim(),
          value: v.value?.trim(),
          stockAdditional: Number(v.stockAdditional || 0),
          priceDelta: Number(v.priceDelta || 0)
        }))
    };

    console.log('Payload final envoyé :', productData);

    if (this.isEditMode && this.productId) {
      this.productService.updateProduct(this.productId, productData).subscribe({
        next: () => this.router.navigate(['/products', this.productId]),
        error: (err) => this.handleError(err)
      });
    } else {
      this.productService.createProduct(productData).subscribe({
        next: (res) => this.router.navigate(['/products', res.id]),
        error: (err) => this.handleError(err)
      });
    }
  }

  private handleError(err: any): void {
    this.submitting = false;
    console.error('Erreur Backend:', err);
    
    // Tenter de récupérer les détails de validation Spring Boot (MethodArgumentNotValidException)
    if (err.error && Array.isArray(err.error.errors)) {
      const messages = err.error.errors.map((e: any) => `${e.field}: ${e.defaultMessage}`);
      this.errorMessage = "Données invalides : " + messages.join(' | ');
    } else if (err.error && typeof err.error === 'object') {
       // Supporte le format Map de base de Spring Boot
       const msg = err.error.message || err.error.error || JSON.stringify(err.error);
       this.errorMessage = "Erreur Backend : " + msg;
    } else {
      this.errorMessage = "Une erreur 400/500 est survenue. Vérifiez la console.";
    }
  }
}
