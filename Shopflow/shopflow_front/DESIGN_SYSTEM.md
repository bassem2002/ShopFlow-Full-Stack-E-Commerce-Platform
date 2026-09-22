# 🎨 Design System & Architecture UI - ShopFlow

Ce document détaille l'architecture visuelle et comportementale de la plateforme ShopFlow. En tant que plateforme e-commerce moderne développée avec **Angular 19** et **Tailwind CSS**, notre objectif est de maintenir une interface utilisateur (UI) cohérente, performante et hautement accessible.

---

## 1. Configuration du Design

Notre approche repose sur l'utilitaire-first (Tailwind) combiné aux variables CSS natives pour une flexibilité maximale.

### 1.1 Variables Globales et Couleurs

Les fondations colorimétriques sont gérées via `tailwind.config.js` et complétées par les variables CSS root (`src/styles.css`).

**Palette de couleurs (Thème Premium) :**
- **Primaire (Indigo 600)** : `#4F46E5` — Utilisé pour les actions principales, les boutons d'appel à l'action et les états actifs.
- **Secondaire (Pink 500)** : `#EC4899` — Utilisé pour les accents visuels (gradients, notifications).
- **Accent (Emerald 500)** : `#10B981` — Indicateur de succès, de stocks disponibles ou de validations.
- **Neutres (Slate)** : Utilisés pour la typographie et les fonds (Dark: `#0F172A`, Light: `#F8FAFC`).

**Implémentation technique :**
```css
/* src/styles.css */
@layer base {
  :root {
    --bg-color: #F8FAFC;
    --text-color: #0F172A;
    --primary: #4F46E5;
    /* ... */
  }
}
```

Ces variables sont étendues dans Tailwind pour permettre l'utilisation de classes utilitaires comme `text-shopflow-primary` ou `bg-shopflow-secondary`.

### 1.2 Typographie et Espacements

**Polices de caractères :**
- **Titres (Headings)** : `Outfit`, choisi pour son côté moderne et géométrique.
- **Corps de texte (Sans-serif)** : `Inter`, optimisé pour la lisibilité sur écran et les interfaces denses.

L'espacement suit l'échelle par défaut de Tailwind CSS (basée sur des multiples de `0.25rem` / `4px`), assurant un rythme vertical régulier. Les coins arrondis (`rounded-xl`, `rounded-2xl`) sont largement utilisés pour donner un aspect "Soft & Premium" (Glassmorphism).

### 1.3 Thématisation (Support du Dark Mode)

Le Dark Mode est géré manuellement par l'utilisateur ou hérité des préférences du système d'exploitation. Nous utilisons la stratégie `darkMode: 'class'` de Tailwind.

- **Mécanisme** : La classe `.dark` est ajoutée/supprimée dynamiquement sur l'élément `<html>` par un service Angular ou le composant racine (`AppComponent`).
- **Utilisation** : Dans les templates, les états sombres sont définis via le préfixe `dark:` (ex: `bg-white dark:bg-slate-900`).

### 1.4 Assets : Icônes et Médias

- **Système d'icônes** : Afin de réduire le poids du bundle et d'éviter des dépendances lourdes (comme Bootstrap Icons), nous utilisons des **icônes SVG en ligne** (type Heroicons). Cela permet d'appliquer directement les classes Tailwind (`w-6 h-6 text-shopflow-primary`) sans perte de qualité.
- **Glassmorphism** : La classe utilitaire personnalisée `.glass` applique nativement un effet de flou (`backdrop-blur`) couplé à une légère transparence pour superposer l'UI sur le contexte.

---

## 2. Événements et Interactions (Design Events)

Une bonne architecture frontend ne se limite pas au visuel statique ; elle englobe la manière dont l'application réagit aux entrées de l'utilisateur.

### 2.1 Feedback Utilisateur

- **Survols et Focus (Hover/Focus)** : Chaque élément interactif (boutons, cartes produit) implémente des micro-interactions.
  - *Cartes* : `hover:-translate-y-1 transition-transform duration-300` pour un léger effet de flottement.
  - *Boutons* : Changement de gradient, et `focus:ring-2 focus:ring-offset-2` pour l'accessibilité au clavier.
- **Chargements (Skeletons & Spinners)** : 
  - Finis les gros spinners bloquants. Nous utilisons des **Skeleton Loaders** asynchrones (`animate-pulse bg-slate-200`) qui reprennent la forme du contenu final pour réduire le temps d'attente perçu (Cumulative Layout Shift minimisé).

### 2.2 Communication UI (Angular Data Flow)

- **Composants Isolés (Smart vs Dumb)** : Les composants visuels (Dumb components, ex: `ProductCardComponent`) communiquent les interactions via `@Output()` (ex: `(addToCart)="handleAddToCart($event)"`).
- **Gestion d'État (Signals & Services)** : 
  - Les interactions globales (ex: ajout au panier) déclenchent un service partagé. 
  - Angular 19 **Signals** ou des `BehaviorSubject` (RxJS) mettent à jour de manière réactive les badges de notification situés dans le Header (`cartItemCount`).
- **Toasts / Notifications** : Les succès d'actions (ex: "Produit ajouté") sont gérés via un composant Overlay injecté dynamiquement, appelé depuis le service pour ne pas polluer l'arbre des composants.

### 2.3 Animations (Transitions d'états et de routes)

- **Micro-animations CSS** : Gérées à 100% via Tailwind (`transition-all duration-200 ease-in-out`). Par exemple, l'ouverture d'un dropdown utilise `animate-fade-in` et `animate-slide-down`.
- **Animations Angular (`@angular/animations`)** :
  - Pour les transitions de pages (Router Outlet), nous implémentons un fondu croisé ou un effet de slide garantissant que l'utilisateur comprend qu'il a changé de contexte.
  - *Exemple de trigger* : Les éléments d'une liste apparaissent de manière échelonnée (Stagger effect) lors du chargement des statistiques du Dashboard.

---

*Document généré et maintenu par l'équipe d'Architecture Frontend. Veillez à toujours consulter ce fichier avant l'ajout d'une nouvelle librairie visuelle afin d'assurer l'homogénéité du projet.*
