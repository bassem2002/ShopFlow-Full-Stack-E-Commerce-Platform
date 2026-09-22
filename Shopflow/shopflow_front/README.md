# ShopFlow Frontend - Angular 19 E-Commerce Application

A modern e-commerce frontend application built with **Angular 19** using **standalone components** and best practices.

## 🎯 Project Overview

ShopFlow Frontend is a comprehensive e-commerce category management system featuring:

- ✅ Angular 19 with standalone components (no NgModules)
- ✅ TypeScript strict mode
- ✅ Reactive Forms for category management
- ✅ Clean and scalable folder structure (core, shared, features)
- ✅ RESTful API integration with HttpClient
- ✅ Hierarchical category tree view
- ✅ Modern UI with responsive design
- ✅ Loading indicators and error handling

## 📁 Project Structure

```
shopflow_front/
├── src/
│   ├── app/
│   │   ├── core/
│   │   │   └── services/
│   │   │       └── category.service.ts       # API service
│   │   ├── shared/
│   │   │   ├── components/
│   │   │   │   ├── loader/                   # Loading indicator
│   │   │   │   └── button/                   # Reusable button
│   │   │   └── models/
│   │   │       └── category.model.ts         # Category interface
│   │   ├── features/
│   │   │   └── categories/
│   │   │       ├── pages/
│   │   │       │   ├── category-list/        # List all categories
│   │   │       │   ├── category-detail/      # View category details
│   │   │       │   ├── category-form/        # Create/Edit category
│   │   │       │   └── category-tree/        # Hierarchical tree view
│   │   │       └── components/
│   │   │           └── category-tree-node/   # Recursive tree node
│   │   ├── app.routes.ts                     # Routing configuration
│   │   ├── app.component.*                   # Root component
│   │   └── main.ts                           # Bootstrap file
│   ├── environments/
│   │   ├── environment.ts                    # Dev environment
│   │   └── environment.prod.ts               # Prod environment
│   ├── index.html
│   └── styles.css                            # Global styles
├── angular.json                              # Angular CLI config
├── tsconfig.json                             # TypeScript config
├── tsconfig.app.json                         # App-specific TS config
├── package.json                              # Dependencies
└── README.md                                 # This file
```

## 🚀 Getting Started

### Prerequisites

- Node.js (v18+)
- npm or yarn
- Angular CLI (v19+)

### Installation

1. **Clone or extract the project:**
```bash
cd shopflow_front
```

2. **Install dependencies:**
```bash
npm install
```

3. **Start the development server:**
```bash
npm start
```

The application will open automatically at `http://localhost:4200`

### Build for Production

```bash
npm run build:prod
```

## 📌 API Endpoints

The application communicates with the backend API at: `http://localhost:8080/api`

### Available Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | Get all categories |
| GET | `/api/categories/:id` | Get category by ID |
| GET | `/api/categories/tree` | Get hierarchical category tree |
| POST | `/api/categories` | Create new category |
| PUT | `/api/categories/:id` | Update category |
| DELETE | `/api/categories/:id` | Delete category |

## 🗂️ Category Model

```typescript
interface Category {
  id: number;
  name: string;
  description?: string;
  parentId?: number;
  children?: Category[];
}
```

## 🛣️ Routes

| Route | Component | Purpose |
|-------|-----------|---------|
| `/categories` | CategoryListComponent | View all categories in a table |
| `/categories/new` | CategoryFormComponent | Create a new category |
| `/categories/:id` | CategoryDetailComponent | View category details |
| `/categories/edit/:id` | CategoryFormComponent | Edit existing category |
| `/categories/tree` | CategoryTreeComponent | View hierarchical category tree |

## 🎨 Features

### Category List
- Display all categories in a responsive table
- View, Edit, and Delete actions
- Show parent category reference
- Loading and error states

### Category Detail
- View complete category information
- Display child categories
- Navigation back to list
- Edit and delete options

### Category Form
- Reactive form with validation
- Create new categories
- Edit existing categories
- Parent category selection
- Form validation and error messages

### Category Tree View
- Recursive tree structure
- Expandable/collapsible nodes
- Quick access to view/edit actions
- Hierarchical visualization of categories

## 🛠️ Technology Stack

- **Framework**: Angular 19
- **Language**: TypeScript (strict mode)
- **Styling**: CSS3 (Responsive design)
- **HTTP Client**: Angular HttpClient
- **Forms**: Reactive Forms
- **Routing**: Angular Router
- **Build Tool**: Angular CLI
- **Package Manager**: npm

## 📝 Key Practices Implemented

✅ **Standalone Components** - No NgModules used
✅ **Dependency Injection** - Using `inject()` function
✅ **Reactive Forms** - FormBuilder and FormGroup
✅ **Error Handling** - Try/catch with observable error handling
✅ **Clean Architecture** - Core, Shared, Features separation
✅ **Reusable Components** - Shared button and loader components
✅ **Environment Configuration** - Environment-based API URLs
✅ **Responsive Design** - Mobile-friendly UI
✅ **Type Safety** - TypeScript strict mode enabled
✅ **Modern Syntax** - Latest Angular features and patterns

## 🔧 Configuration

### Environment Variables

Update the API URL in the environment files:

**src/environments/environment.ts** (Development)
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
};
```

**src/environments/environment.prod.ts** (Production)
```typescript
export const environment = {
  production: true,
  apiUrl: 'http://your-api-domain.com/api',
};
```

## 📦 Installation & Deployment

### Local Development

```bash
# Install dependencies
npm install

# Start dev server
npm start

# The app opens at http://localhost:4200
```

### Build & Deploy

```bash
# Production build
npm run build:prod

# Output in dist/shopflow_front/
# Deploy these files to your web server
```

## 🎯 Development Guidelines

### Adding New Features

1. Create components in appropriate feature folder
2. Use standalone components with `@Component` standalone: true
3. Inject dependencies using `inject()`
4. Follow the folder structure pattern
5. Create services in the core folder for API calls
6. Use reactive forms for user input

### Best Practices

- Use strong typing throughout
- Avoid any types
- Leverage signals when appropriate
- Handle errors properly with try/catch
- Always unsubscribe or use async pipe
- Keep components focused and reusable
- Write meaningful template comments

## 🐛 Troubleshooting

### API Connection Issues

If you get CORS errors:
1. Ensure backend is running on port 8080
2. Check API URL in environment.ts
3. Verify backend CORS configuration

### Module Not Found Errors

Ensure all imports are correct:
```typescript
import { Component } from '@angular/core';
import { CategoryService } from '@core/services/category.service';
```

## 📞 Support

For issues or questions, please check:
- Angular Documentation: https://angular.io
- TypeScript Documentation: https://www.typescriptlang.org
- RxJS Documentation: https://rxjs.dev

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Happy Coding! 🚀**

Built with ❤️ using Angular 19
