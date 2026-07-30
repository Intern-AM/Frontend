# BuzzHive - Web Automation & Reviewer Dashboard

[![React](https://img.shields.io/badge/React-18.2-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5.1-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vitejs.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.2-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4.3-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)
[![Vercel](https://img.shields.io/badge/Vercel-Deployment_Ready-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://vercel.com/)

**BuzzHive Web** is an enterprise-grade, high-performance web application built for automated social media campaign management, event syncing, multi-channel approval workflows, and OAuth token administration.

Designed with a sleek **3D Glassmorphism** design system, the application maintains 100% contract parity with the BuzzHive backend REST API (`https://debian.tail72ffe0.ts.net/`) and Android mobile application (`SpeehiveApiService.kt`).

---

## Table of Contents

- [Project Overview](#project-overview)
- [Core Tech Stack](#core-tech-stack)
- [Codebase Architecture and Directory Structure](#codebase-architecture-and-directory-structure)
- [Entry Files and Configuration Analysis](#entry-files-and-configuration-analysis)
- [Environment Variables](#environment-variables)
- [Step-by-Step Local Setup and Installation Guide](#step-by-step-local-setup-and-installation-guide)
- [Usage Examples and Workflows](#usage-examples-and-workflows)
- [API Endpoint Reference Matrix](#api-endpoint-reference-matrix)
- [Security Architecture and Vercel Deployment](#security-architecture-and-vercel-deployment)

---

## Project Overview

The BuzzHive Web Platform acts as the central web dashboard for social media automation across organizations. Key operational capabilities include:

* **Real-time Campaign Dashboard**: Summary stats (**Active Events**, **Pending Approval**, **Posted Events**), organization metrics, and an integrated Admin settings panel.
* **Unified Event Review Feed**: Inspect AI-generated post copy, hashtags, prompts, and poster graphics; trigger 1-click approvals or reject with reviewer notes directly from a unified feed.
* **Sliding Status Filters**: Instantly filter campaigns by Month, Year, and Status (All, Pending, Generated, Rejected, Completed) with custom sliding category selectors.
* **Bulk Event Ingestion**: Administrators can upload Excel files (`.xlsx`) to ingest events in bulk, with built-in layout validation checks and a validation error dialog.
* **Multi-Platform Publishing Schedules**: Configure and update publish timestamps independently for **LinkedIn**, **Instagram**, **MS Teams**, and **WhatsApp**.
* **OAuth Credentials and Token Expiration Manager (Admin Only)**: Update social media API tokens, set expiration dates, toggle channel active status, and track automated 7-day expiration warnings.
* **User Administration and RBAC**: Create and manage **Admin** and **Reviewer** accounts with instant activation/deactivation toggles.
* **Compliance Audit Logs**: Searchable audit trail capturing administrative activities, role updates, and campaign actions.
* **Interactive Media Preview**: High-resolution image lightbox featuring multi-level zooming and smooth click-and-drag panning.
* **UX Enhancements**: Integrated pull-to-refresh gestures, action confirmation dialogs, automatic session logouts on token expiry, and page/editor lock rules for archived or posted events.

---

## Core Tech Stack

| Layer | Technology | Details / Version |
| :--- | :--- | :--- |
| **Frontend Library** | **React** | `v18.2.0` — Component-driven UI development |
| **Build Tooling** | **Vite** | `v5.1.6` — Lightning-fast HMR & production bundler |
| **Language** | **TypeScript** | `v5.2.2` — Strict type checking & API contract models |
| **Styling Framework** | **Tailwind CSS** | `v4.3.3` + `@tailwindcss/vite` — Utility-first modern CSS |
| **HTTP Client** | **Axios** | `v1.6.8` — Request/Response interceptors & JWT auth |
| **Icons** | **Lucide React** | `v0.344.0` — Clean vector icons |
| **Deployment Edge** | **Vercel** | SPA routing, edge API proxying, and security headers |

---

## Codebase Architecture and Directory Structure

```
speehiveweb/
├── public/
│   ├── favicon.png                  # Application browser favicon
│   ├── hive_logo.png                # High-res brand logo asset
│   └── manifest.webmanifest         # Progressive Web App (PWA) manifest
├── src/
│   ├── api/
│   │   ├── client.ts                # Axios client instance, JWT interceptors & URL helpers
│   │   └── events.ts                # Event and campaign API calls
│   ├── components/
│   │   ├── admin/
│   │   │   └── BulkEventImport.tsx  # Ingest events in bulk via Excel upload
│   │   ├── ApiConnectionBanner.tsx  # Compact header connection status pill badge
│   │   ├── ConfirmationDialog.tsx   # Reusable operational confirmation dialog
│   │   ├── ErrorBoundary.tsx        # React error boundary fallback UI
│   │   ├── ImageLightboxModal.tsx   # Lightbox preview modal with zoom & drag-pan
│   │   ├── ImagePromptCard.tsx      # AI image generation prompt inspector
│   │   ├── ImageUploadModal.tsx     # Custom campaign & event poster upload modal
│   │   ├── Navbar.tsx               # Responsive glassmorphic navigation header
│   │   ├── PullToRefresh.tsx        # Gesture-based panel refresh wrapper
│   │   ├── RoleGuard.tsx            # Page-level Role-Based Access Control wrapper
│   │   ├── StatusBadge.tsx          # Color-coded status pill badges
│   │   ├── UpdateCredentialModal.tsx # API key, expiration date & active switch modal
│   │   └── ViewModeSwitcher.tsx     # Grid vs Compact campaign view layout toggle
│   ├── context/
│   │   ├── AuthContext.tsx          # JWT token management, user role & login state
│   │   └── ToastContext.tsx         # Toast notification system
│   ├── pages/
│   │   ├── AuditLogs.tsx            # Compliance system audit log viewer
│   │   ├── CampaignDetail.tsx       # Comprehensive campaign inspection & schedule editor
│   │   ├── Dashboard.tsx            # Dashboard metrics & Admin settings
│   │   ├── Events.tsx               # Synced event feed, sliding status filters, and calendar
│   │   ├── Login.tsx                # Workspace authentication screen with offline protection
│   │   ├── Notifications.tsx        # Real-time notification center & posting status
│   │   └── UserAdmin.tsx            # Admin user account management & role metrics
│   ├── types/
│   │   ├── events.ts                # Event-specific TypeScript types
│   │   └── index.ts                 # Core TypeScript interfaces & DTO definitions
│   ├── utils/
│   │   ├── date.ts                  # Timezone (IST) and date helpers
│   │   ├── error.ts                 # Error parsing and normalization helpers
│   │   ├── poster.ts                # Poster asset path utilities
│   │   └── schedule.ts              # Publishing schedule validators
│   ├── App.tsx                      # Main layout shell, SPA state router & Error Boundary
│   ├── index.css                    # CSS design tokens & Tailwind CSS v4 directives
│   ├── main.tsx                     # React application entry point
│   └── vite-env.d.ts                # Vite environment type declarations
├── index.html                       # HTML5 entry template & CSP security meta rules
├── package.json                     # Project metadata, scripts & dependencies
├── tsconfig.json                    # TypeScript master compiler configuration
├── tsconfig.app.json                # Frontend application TS config
├── tsconfig.node.json               # Vite node environment TS config
├── vercel.json                      # Vercel deployment rewrites, API proxy & security headers
├── vite.config.ts                   # Vite server proxy & plugin configuration
└── README.md                        # Project documentation
```

---

## Entry Files and Configuration Analysis

### 1. `index.html` (Application Document Entry)
Serves as the root HTML5 document template. Defines:
* **Content-Security-Policy (CSP)** meta tag restricting script, font, style, and API connection origins.
* Google Fonts preconnect links for **Space Grotesk** (headings), **Lilex** (code/mono), and **Inter** (body).
* Mount container `<div id="root"></div>` and entry script module `/src/main.tsx`.

### 2. `src/main.tsx` (React Entry Point)
Initializes the React virtual DOM:
* Mounts `<App />` within `React.StrictMode`.
* Wraps the application in `<ToastProvider>` and `<AuthProvider>` to provide global state.

### 3. `src/App.tsx` (Routing Shell & Error Boundary)
* Manages top-level SPA state-based routing (`dashboard`, `events`, `campaign-detail`, `notifications`, `audit-logs`, `users`).
* Wraps all active pages in a global `<ErrorBoundary>` to trap rendering exceptions gracefully.
* Applies `<RoleGuard requiredRole="Admin">` to restrict sensitive admin tabs (`users`, `audit-logs`).

### 4. `package.json` (Dependency & Script Manifest)
Key scripts:
```json
"scripts": {
  "dev": "vite",
  "build": "tsc && vite build",
  "preview": "vite preview"
}
```

### 5. `vercel.json` (Deployment & Security Configuration)
Configured for zero-config Vercel deployments:
* **SPA Catch-all Rewrite**: `/(.*)` -> `/index.html`
* **API Edge Proxy**: `/api/:path*` -> `https://debian.tail72ffe0.ts.net/api/:path*`
* **HTTP Security Headers**: `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Strict-Transport-Security` (HSTS preload), `Permissions-Policy`.

---

## Environment Variables

Environment variables are managed via Vite's `import.meta.env` system.

### Variable Reference Table

| Variable Name | Type | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `VITE_API_BASE_URL` | `string` | `https://debian.tail72ffe0.ts.net/` | Target backend REST API server origin |

### Example `.env` File Configuration
Create a `.env` file in the root directory:

```env
# Production Backend REST API Origin
VITE_API_BASE_URL=https://debian.tail72ffe0.ts.net
```

---

## Step-by-Step Local Setup and Installation Guide

### Prerequisites
Ensure your local environment meets the following requirements:
* **Node.js**: `v18.0.0` or higher
* **npm**: `v9.0.0` or higher
* **Git**: Installed and configured

### 1. Clone the Repository & Checkout Target Web Branch
```bash
git clone https://github.com/Intern-AM/Frontend.git
cd Frontend
git checkout feature/web-app
```

### 2. Install Project Dependencies
Run `npm install` to install React 18, Vite 5, Tailwind CSS v4, Lucide React, and Axios:
```bash
npm install
```

### 3. Configure Environment Variables (Optional)
If connecting to a custom backend environment, create a `.env` file:
```bash
cp .env.example .env
```

### 4. Start the Development Server
Launch Vite's local development server with Hot Module Replacement (HMR):
```bash
npm run dev
```
The application will be available at **`http://localhost:5173`**.

### 5. Build for Production Verification
Validate TypeScript types and build the production bundle:
```bash
npm run build
```
Production artifacts will be compiled into the `dist/` directory.

### 6. Preview Production Build Locally
```bash
npm run preview
```

---

## Usage Examples and Workflows

### Example 1: Authenticating & Switching Roles

```typescript
import { useAuth } from './context/AuthContext';

const Component = () => {
  const { user, role, login, logout } = useAuth();

  // Sign in as Reviewer or Admin
  const handleSignIn = () => {
    login('jwt-token-string', 'admin_user', 'Admin');
  };

  return (
    <div>
      <p>Current Role: {role}</p>
      {role === 'Admin' && <button>Access Admin Control Panel</button>}
    </div>
  );
};
```

### Example 2: Approving or Rejecting Campaigns

```typescript
import { apiClient } from './api/client';

// 1. Approve Campaign
const approveCampaign = async (eventId: string) => {
  await apiClient.post('/api/Approval/approve', {
    eventId: eventId,
    comments: 'Content approved for scheduling',
  });
};

// 2. Reject Campaign with Notes
const rejectCampaign = async (eventId: string, reason: string) => {
  await apiClient.post('/api/Approval/reject', {
    eventId: eventId,
    comments: reason,
  });
};
```

### Example 3: Updating Social Media OAuth Token & Expiration Date

```typescript
import { apiClient } from './api/client';

const updateLinkedInToken = async (accessToken: string, expiresAtDate: string) => {
  await apiClient.put('/api/SocialMediaCredentials/LinkedIn', {
    accessToken: accessToken,
    expiresAt: new Date(expiresAtDate).toISOString(), // "2026-08-15T00:00:00.000Z"
    isActive: true,
  });
};
```

### Example 4: Updating Platform Publishing Schedule

```typescript
import { apiClient } from './api/client';

const updateInstagramSchedule = async (eventId: string, scheduledIsoTime: string) => {
  await apiClient.put(`/api/Approval/${eventId}/schedule/Instagram`, {
    scheduledTime: scheduledIsoTime,
  });
};
```

---

## API Endpoint Reference Matrix

| Feature | Method | Endpoint Path | Payload / Query | Access Level |
| :--- | :--- | :--- | :--- | :--- |
| **Authentication** | `POST` | `/api/Auth/login` | `{ username, email, password }` | Public |
| **Campaign Listing** | `GET` | `/api/Campaigns` | None | All Roles |
| **Campaign Post Edit** | `PUT` | `/api/Campaigns/{eventId}` | `{ campaignPost, hashtags }` | Reviewer / Admin |
| **Campaign Poster Upload**| `POST` | `/api/Campaigns/{eventId}/image` | `FormData(image)` | Reviewer / Admin |
| **Event Calendar List** | `GET` | `/api/Events` | None | All Roles |
| **Event Cancellation** | `PUT` | `/api/Events/{id}/cancel` | None | Reviewer / Admin |
| **Event Poster Upload** | `POST` | `/api/Events/{eventId}/image` | `FormData(image)` | Reviewer / Admin |
| **Approve Campaign** | `POST` | `/api/Approval/approve` | `{ eventId, comments }` | Reviewer / Admin |
| **Reject Campaign** | `POST` | `/api/Approval/reject` | `{ eventId, comments }` | Reviewer / Admin |
| **Fetch Schedule** | `GET` | `/api/Approval/{eventId}/schedule` | None | All Roles |
| **Update Schedule** | `PUT` | `/api/Approval/{eventId}/schedule/{platform}` | `{ scheduledTime }` | Reviewer / Admin |
| **Get API Credentials** | `GET` | `/api/SocialMediaCredentials` | None | Admin Only |
| **Update API Credential**| `PUT` | `/api/SocialMediaCredentials/{provider}` | `{ accessToken, expiresAt, isActive }` | Admin Only |
| **List Users** | `GET` | `/api/Admin/users` | None | Admin Only |
| **Create User** | `POST` | `/api/Admin/users` | `{ name, username, email, password, role }` | Admin Only |
| **Activate Account** | `PUT` | `/api/Admin/users/{id}/activate` | None | Admin Only |
| **Deactivate Account** | `PUT` | `/api/Admin/users/{id}/deactivate` | None | Admin Only |
| **Fetch Audit Logs** | `GET` | `/api/Admin/auditlogs` | None | Admin Only |

---

## Security Architecture and Vercel Deployment

### Security Hardening Features
1. **HTTP Security Headers (`vercel.json`)**:
   - `X-Frame-Options: DENY` (Anti-clickjacking)
   - `X-Content-Type-Options: nosniff` (Anti-MIME sniffing)
   - `Strict-Transport-Security: max-age=63072000; includeSubDomains; preload` (HSTS)
   - `Permissions-Policy: camera=(), microphone=(), geolocation=(), payment=()`
2. **Content Security Policy (`index.html` & `vercel.json`)**:
   - Strictly limits script, style, font, and image source domains.
3. **Role-Based Access Control (`RoleGuard.tsx`)**:
   - Guards routes client-side, showing a 3D `403 Access Denied` fallback for unauthorized roles.

---

## License and Credits

Copyright © 2026 **BuzzHive Team / Intern-AM**. All rights reserved.
