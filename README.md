# 🐝 Hive AI — Web Automation & Reviewer Dashboard

**Hive AI Web** is a modern, high-performance web application designed for intelligent social media campaign management, event syncing, content reviews, and automated social media API key administration.

Built with **React 18**, **Vite**, **TypeScript**, and **Tailwind CSS**, it operates in 100% parity with the Speehive backend REST API and the Android mobile application.

---

## 🌟 Key Features

### 📊 1. Dashboard Overview
* **Metric Quick Stats**: Real-time counts for **Active Events**, **Pending Approval** (Campaign Queue), and **Posted Events**.
* **Campaign Queue**: Instant review cards for generated and approved campaigns awaiting reviewer action.
* **Upcoming Events**: Calendar schedule cards synced live from backend events.
* **Admin Social Media Credentials Manager**: Integrated OAuth token management card for Admins with expiration alerts and active status toggles.

### 📢 2. Campaign Management & Detailed Review
* **Multi-View Modes**: Switch between **Grid** and **Compact** view layouts.
* **Full Campaign Inspection**: View and edit campaign post copy, hashtags, CTAs, AI image generation prompts, and custom poster graphics.
* **Approval & Rejection Workflow**: One-click campaign approval or modal rejection with reviewer comments.
* **Platform Posting Schedules**: Configure and update publish times for individual social channels (**LinkedIn**, **Instagram**, **MS Teams Group**, **WhatsApp Channel**).

### 📅 3. Event Management & Calendar Syncing
* **Event Administration**: Browse synced events, inspect locations, start/end times, and event categories.
* **Event Cancellation**: Cancel events with automated notification cascades.
* **Poster Media Sync**: Upload custom poster graphics linked directly to event IDs.

### 🔔 4. Notification Center
* **Live Activity Feed**: Filtered notification cards for approved campaigns, published postings, and event cancellations.
* **Platform Posting Breakdown**: Accordion cards displaying per-channel posting status (`Posted`, `Scheduled`, `Pending`) for approved and published campaigns.
* **Token Expiration Warnings**: Automated alerts for API credentials expiring within 7 days.

### 🔑 5. API Credentials & Token Expiration Configuration (Admin Only)
* **Token & Key Administration**: Update OAuth access tokens for LinkedIn, Instagram, MS Teams, and WhatsApp.
* **Expiration Date Selector**: Input expiration dates (`<input type="date">`) with automated countdown timers (`⏰ X days remaining`).
* **Active Status Switch**: Enable or pause automated posting for individual social channels.
* **Warning Badges**: `EXPIRING SOON` (amber) and `EXPIRED` (red) badges on credential cards and dashboard top banners.

### 👥 6. User Administration & RBAC (Admin Only)
* **User Management**: Create new **Admin** or **Reviewer** user accounts.
* **Account Toggles**: Instantly activate or deactivate user platform access.
* **Metrics Cards**: Summary metrics for Total Users, Active Users, Reviewers, and Admins.

### 🛡️ 7. System Audit Logs (Admin Only)
* **Compliance Activity Feed**: Searchable log feed tracking administrative actions, campaign approvals, user creation, and status updates.
* **Search & Filter**: Real-time search by user, action type, or details string.

---

## 🛠️ Technology Stack

* **Framework**: [React 18](https://react.dev/) + [Vite](https://vitejs.dev/)
* **Language**: [TypeScript](https://www.typescriptlang.org/)
* **Styling**: [Tailwind CSS v4](https://tailwindcss.com/) + Custom 3D Glassmorphism System
* **Icons**: [Lucide React](https://lucide.dev/)
* **HTTP Client**: [Axios](https://axios-http.com/) (with automatic JWT authentication interceptors)
* **Routing**: Lightweight Single-Page Application (SPA) state router with `<RoleGuard>` protection

---

## 🌐 Backend API Integration

The web app connects to the production backend server:
* **Backend Base URL**: `https://debian.tailbd6bc8.ts.net/`
* **API Edge Proxy (Vercel)**: Configured in `vercel.json` (`/api/:path*` -> `https://debian.tailbd6bc8.ts.net/api/:path*`)

### Core API Endpoints

| Resource | Endpoint Path | Method | Access Level | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Auth** | `/api/Auth/login` | `POST` | Public | User authentication & JWT issuance |
| **Campaigns** | `/api/Campaigns` | `GET` | All | Fetch live campaign list |
| **Campaign Edit** | `/api/Campaigns/{eventId}` | `PUT` | Reviewer / Admin | Update campaign post copy & hashtags |
| **Campaign Image** | `/api/Campaigns/{eventId}/image` | `POST` | Reviewer / Admin | Upload custom campaign poster graphic |
| **Events** | `/api/Events` | `GET` | All | Fetch synced event calendar |
| **Event Cancel** | `/api/Events/{id}/cancel` | `PUT` | Reviewer / Admin | Mark event as cancelled |
| **Event Image** | `/api/Events/{eventId}/image` | `POST` | Reviewer / Admin | Upload event poster graphic |
| **Approvals** | `/api/Approval/approve` | `POST` | Reviewer / Admin | Approve campaign for publishing |
| **Rejections** | `/api/Approval/reject` | `POST` | Reviewer / Admin | Reject campaign with reviewer notes |
| **Schedules** | `/api/Approval/{eventId}/schedule` | `GET` | All | Fetch platform publishing schedules |
| **Schedule Update** | `/api/Approval/{eventId}/schedule/{platform}` | `PUT` | Reviewer / Admin | Update scheduled time for platform |
| **Social Credentials**| `/api/SocialMediaCredentials` | `GET` | Admin Only | Fetch social media OAuth token status |
| **Update Credential** | `/api/SocialMediaCredentials/{provider}` | `PUT` | Admin Only | Update access token, expiration & active status |
| **User Admin** | `/api/Admin/users` | `GET`, `POST` | Admin Only | List or create user accounts |
| **User Status** | `/api/Admin/users/{id}/activate` | `PUT` | Admin Only | Activate user account |
| **Audit Logs** | `/api/Admin/auditlogs` | `GET` | Admin Only | Fetch compliance activity logs |

---

## 🔒 Security & Role-Based Access Control (RBAC)

1. **Role Protection (`RoleGuard.tsx`)**:
   - Navigation items and pages (`User Admin`, `Audit Logs`, `Social Media Credentials`) are strictly guarded for `Admin` accounts.
   - Non-admin users attempting to access protected views receive a styled `403 Access Denied` warning banner.
2. **Security Headers (`vercel.json`)**:
   - `X-Frame-Options: DENY` (Prevents clickjacking)
   - `X-Content-Type-Options: nosniff` (Prevents MIME-sniffing)
   - `Strict-Transport-Security` (Enforces HTTPS)
   - `Permissions-Policy` (Restricts geolocation, camera, microphone)
3. **Content Security Policy (`index.html`)**:
   - Restricts script execution and resource loading strictly to trusted origins.
4. **React Error Boundary (`ErrorBoundary.tsx`)**:
   - Global crash protection UI with one-click recovery.

---

## 💻 Getting Started

### Prerequisites
* **Node.js**: `v18.0.0` or higher
* **npm**: `v9.0.0` or higher

### 1. Clone the Repository & Checkout Branch
```bash
git clone https://github.com/Intern-AM/Frontend.git
cd Frontend
git checkout feature/web-app
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Environment Setup (Optional)
Create a `.env` file in the project root:
```env
VITE_API_BASE_URL=https://debian.tailbd6bc8.ts.net
```

### 4. Run Development Server
```bash
npm run dev
```
Open **`http://localhost:5173`** in your browser.

### 5. Build for Production
```bash
npm run build
```
Generates optimized static assets in `dist/`.

---

## ⚡ Deployment to Vercel

This repository is fully configured for zero-setup deployment on [Vercel](https://vercel.com).

### Deployment Steps:
1. Import the repository `https://github.com/Intern-AM/Frontend` on Vercel.
2. Select branch: **`feature/web-app`**.
3. Framework Preset: **Vite**.
4. Build Command: `npm run build`.
5. Output Directory: `dist`.
6. Click **Deploy**!

`vercel.json` automatically configures SPA routing (`/(.*)` -> `/index.html`), API edge proxying, and HTTP security headers.

---

## 📁 Directory Structure

```
speehiveweb/
├── public/
│   ├── favicon.ico
│   ├── hive_logo.png
│   └── manifest.webmanifest
├── src/
│   ├── api/
│   │   └── client.ts              # Axios client & origin URL resolvers
│   ├── components/
│   │   ├── ApiConnectionBanner.tsx # Header connection status pill badge
│   │   ├── ErrorBoundary.tsx      # React error boundary component
│   │   ├── ImageLightboxModal.tsx  # Fullscreen image viewer
│   │   ├── ImagePromptCard.tsx    # AI prompt inspector component
│   │   ├── ImageUploadModal.tsx   # Custom poster upload modal
│   │   ├── Navbar.tsx             # Responsive glassmorphic navigation bar
│   │   ├── RoleGuard.tsx          # RBAC page wrapper component
│   │   ├── StatusBadge.tsx        # Styled status pill badges
│   │   ├── UpdateCredentialModal.tsx # API key & expiration date picker modal
│   │   └── ViewModeSwitcher.tsx   # Grid vs Compact layout toggle
│   ├── context/
│   │   ├── AuthContext.tsx        # Auth state management & role provider
│   │   └── ToastContext.tsx       # Global toast notification manager
│   ├── pages/
│   │   ├── AuditLogs.tsx          # System compliance audit logs
│   │   ├── CampaignDetail.tsx     # Detailed campaign review & schedule editor
│   │   ├── Campaigns.tsx          # Active campaign listing page
│   │   ├── Dashboard.tsx          # Dashboard overview & admin credentials manager
│   │   ├── Events.tsx             # Event calendar & cancellation manager
│   │   ├── Login.tsx              # Workspace sign-in screen
│   │   ├── Notifications.tsx      # Real-time notification center
│   │   └── UserAdmin.tsx          # User account & role management
│   ├── types/
│   │   └── index.ts               # TypeScript interfaces & type definitions
│   ├── App.tsx                    # Main SPA layout & router component
│   ├── index.css                  # Design system tokens & Tailwind CSS v4 setup
│   └── main.tsx                   # React application entry point
├── index.html                     # HTML5 template & CSP headers
├── package.json                   # Project dependencies & scripts
├── tsconfig.json                  # TypeScript compiler configuration
├── vercel.json                    # Vercel deployment & security header config
└── README.md                      # Project documentation
```

---

## 📄 License

Copyright © 2026 **Hive AI Team**. All rights reserved.
