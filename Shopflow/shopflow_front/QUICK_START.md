# Quick Start Guide - ShopFlow Frontend

## 📋 Prerequisites

Ensure you have the following installed:
- **Node.js** v18+ ([Download](https://nodejs.org/))
- **npm** (comes with Node.js)
- **Git** (for version control)

## 🚀 Setup Instructions

### Step 1: Navigate to Project Directory
```bash
cd "c:\Users\GMI\Desktop\iit\semestre 2\jee-spring\shopflow_front"
```

### Step 2: Install Dependencies
```bash
npm install
```
This will install all required packages listed in `package.json`.

### Step 3: Start Development Server
```bash
npm start
```
This command will:
- Compile the Angular application
- Start the dev server on `http://localhost:4200`
- Automatically open the app in your default browser

### Step 4: Access the Application
Open your browser and navigate to:
```
http://localhost:4200
```

## 📝 Main Routes

- **Categories List**: http://localhost:4200/categories
- **Create Category**: http://localhost:4200/categories/new
- **Category Details**: http://localhost:4200/categories/1 (replace 1 with category ID)
- **Edit Category**: http://localhost:4200/categories/edit/1
- **Category Tree**: http://localhost:4200/categories/tree

## 🔌 Backend API Setup

Ensure your backend is running on:
```
http://localhost:8080/api
```

To change the API URL, edit:
```
src/environments/environment.ts
```

## 📁 Project Structure Overview

```
src/
├── app/
│   ├── core/              # Services and business logic
│   ├── shared/            # Reusable components and models
│   └── features/          # Feature modules (categories)
├── environments/          # Environment-specific configs
├── index.html             # Main HTML file
└── styles.css             # Global styles
```

## 🛠️ Common Commands

| Command | Purpose |
|---------|---------|
| `npm start` | Start dev server |
| `npm build` | Build for development |
| `npm run build:prod` | Build for production |
| `npm watch` | Build in watch mode |
| `npm test` | Run unit tests |

## 🐛 Troubleshooting

### Port 4200 Already in Use
```bash
ng serve --port 4201
```

### Clear Node Modules Cache
```bash
rm -rf node_modules
npm install
```

### Clear Angular Cache
```bash
rm -rf .angular/cache
npm start
```

## 📚 File Structure Explained

- **main.ts** - Application bootstrap file
- **app.routes.ts** - Routing configuration
- **app.component.ts** - Root component
- **category.service.ts** - API service for categories
- **category.model.ts** - Category interface definition

## 🔐 TypeScript Strict Mode

This project uses TypeScript strict mode for maximum type safety:
```typescript
"strict": true
```

Ensure all code is properly typed to avoid compilation errors.

## 📦 Deployment

### Build for Production
```bash
npm run build:prod
```

### Deploy Output
The built files are in:
```
dist/shopflow_front/
```

Upload these files to your web server (Apache, Nginx, etc.).

## 🎨 UI Components Used

- **LoaderComponent** - Loading spinner
- **ButtonComponent** - Reusable button with variants
- **CategoryTreeNodeComponent** - Recursive tree node

## 📖 Documentation

- [Angular Documentation](https://angular.io/docs)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [RxJS Documentation](https://rxjs.dev/)

## ✅ Verification Checklist

- [ ] Node.js installed
- [ ] Dependencies installed (`npm install` completed)
- [ ] Backend running on `http://localhost:8080`
- [ ] Development server started (`npm start`)
- [ ] Application accessible at `http://localhost:4200`
- [ ] API calls working (check browser console)
- [ ] Categories loading in the list

## 💡 Next Steps

1. Verify the backend API is running
2. Test the category list page
3. Try creating a new category
4. Explore the category tree view
5. Test edit and delete functionality

---

**Happy Coding!** 🎉

For more details, see [README.md](README.md)
