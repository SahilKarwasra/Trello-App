# 📌 Trello App — Real-Time Collaborative Kanban Board

A modern, high-performance, full-stack collaborative Kanban board inspired by Trello, Jira, and Linear. Built with a shared **Kotlin Multiplatform (Compose Multiplatform)** frontend targeting **Android, iOS, and Desktop (macOS / Windows / Linux)**, powered by a **Go (Golang)** backend with concurrent room-based **WebSockets** and **PostgreSQL**.

---

## 🎨 The Engineering Story & AI Collaboration

> *"The future of software development is blending human architecture with AI agility."*

This repository represents a fusion of rigorous manual systems engineering and cutting-edge generative AI:

- 🤖 **Frontend UI & Presentation Layer — Vibe Coded with AI:**
  The complete visual presentation layer — including the bold **Neo-Brutalist** aesthetic (stark borders, offset geometric drop shadows, dynamic color badges, tactile elevation layers), custom gesture physics, auto-scrolling column drag-and-drop animations, and responsive screen layouts — was created through iterative **AI-assisted "vibe coding"**. Generative AI was leveraged to rapidly prototype, refine, and polish an interface that feels alive, playful, and responsive.

- 🛠️ **Frontend Core Architecture & Infrastructure — 100% Handcrafted:**
  All foundational engineering was built meticulously by hand: the multi-target Kotlin Multiplatform architecture, Jetpack DataStore multiplatform persistence, Ktor HTTP & WebSocket engine integration, Koin Dependency Injection graph, MVI (Model-View-Intent) unidirectional data flow, type-safe Navigation Compose routes, and platform-specific window insets.

- ⚡ **Backend Engine & Real-Time Hub — 100% Handcrafted:**
  The entire backend was engineered by hand from the ground up in Go. It implements Clean Architecture across Go workspaces (`apps/api`, `packages/database`, `packages/config`), GORM schema migrations, JWT security middleware, and a concurrent, channel-driven WebSocket Hub managing isolated board rooms, user presence detection (`USER_JOINED`, `USER_LEFT`, `ROOM_STATE`), and real-time board mutation broadcasts.

---

## 🌟 Key Features

- **Multiplatform Everywhere:** Run natively on **Android**, **Desktop (macOS, Windows, Linux)**, and **iOS** from a single Kotlin Compose codebase.
- **Neo-Brutalist Design System:** Distinctive visual style featuring bold black borders (2–2.5dp), hard offset drop shadows, high-contrast surface palettes, and crisp micro-interactions.
- **Real-Time Collaboration:** Board updates (adding cards, updating titles, moving columns, deleting items) synchronize across all connected users with sub-millisecond WebSocket delivery.
- **Live User Presence:** Dynamic room presence showing how many users are active on the board, who is currently online, and instant notifications when collaborators join or leave.
- **Physics-Inspired Drag & Drop:** Custom long-press gesture handling for both **Issue Cards** and entire **Columns/Sections**, complete with boundary detection, smooth auto-scrolling on horizontal board edges, and live card previews.
- **Organization & Multi-Tenancy:** Multi-tenant workspace model where users create or join organizations, manage boards within organizations, and invite team members.
- **Rock-Solid Session Management:** Automatic token refreshing, session expiry interception, and safe credential clearing with Jetpack DataStore.
- **Mobile Edge-to-Edge:** Native status bar, navigation bar, and display cutout handling across Android and iOS.

---

## 🏗️ System Architecture

### High-Level Architecture Overview

```mermaid
flowchart TD
    subgraph Clients["Cross-Platform Clients (Compose Multiplatform)"]
        A1[Android App]
        A2[Desktop App]
        A3[iOS App]
    end

    subgraph FrontendShared["Frontend Shared Core (KMP)"]
        UI[Neo-Brutalist Compose UI]
        VM[MVI ViewModels]
        Repo[Data Repositories]
        WS_Client[Ktor WebSocket Manager]
        HTTP_Client[Ktor HTTP Client]
        DS[Jetpack DataStore]
    end

    subgraph GoBackend["Backend Engine (Go / Gin)"]
        Router[Gin Engine Router]
        AuthMid[JWT Auth Middleware]
        Handlers[HTTP Handlers]
        Services[Business Logic & Permissions]
        DB_Repo[Database Repositories]
        WSHub[Concurrent WebSocket Hub]
    end

    subgraph Storage["Persistent Storage"]
        PG[(PostgreSQL Database)]
    end

    Clients --> UI
    UI <--> VM
    VM <--> Repo
    Repo <--> DS
    Repo <--> HTTP_Client
    Repo <--> WS_Client

    HTTP_Client -- REST APIs (JSON) --> Router
    WS_Client -- WS Protocol (?board_id & ?token) --> Router

    Router --> AuthMid
    AuthMid --> Handlers
    Router --> WSHub
    Handlers --> Services
    Services --> DB_Repo
    DB_Repo --> PG
    Services -- Broadcast Events --> WSHub
    WSHub -- Real-time Board Events --> WS_Client
```

---

## 💻 Tech Stack

### ⚡ Backend (Go / Gin / PostgreSQL)

| Technology | Purpose |
|---|---|
| **Go 1.24+** | High-performance, concurrent language runtime |
| **Gin Gonic** | Fast, ergonomic HTTP web routing framework |
| **Gorilla WebSocket** | Connection upgrading, read/write pumps, and socket management |
| **GORM** | ORM for PostgreSQL with schema automigrations |
| **PostgreSQL** | Relational data store with foreign key cascades and UUID primary keys |
| **golang-jwt (v5)** | Secure HMAC-SHA256 stateless authentication tokens |
| **Go Workspaces (`go.work`)** | Monorepo multi-module configuration |


### 📱 Frontend (Shared Multiplatform)

| Technology | Purpose |
|---|---|
| **Kotlin Multiplatform (KMP)** | Shared core business logic, models, and network code |
| **Compose Multiplatform (1.12+)** | Declarative, cross-platform UI for Android, Desktop, and iOS |
| **Ktor Client (3.6+)** | Multiplatform HTTP client & asynchronous WebSockets engine |
| **Koin (4.2+)** | Dependency injection across shared and platform modules |
| **Jetpack Navigation Compose** | Type-safe navigation with Kotlin Serialization destinations |
| **Jetpack DataStore Preferences** | Multiplatform encrypted/persistent credential storage |
| **Kotlinx Coroutines & Flow** | Asynchronous reactive streams and state handling |
| **Kotlinx Serialization** | High-performance JSON serialization |

---

## 📂 Project Structure

```
Trello-App/
├── Makefile                     # Root build, run, and migration targets
├── go.work                      # Go multi-module workspace
├── go.work.sum
│
├── apps/
│   ├── api/                     # Go REST & WebSocket Backend
│   │   ├── cmd/
│   │   │   ├── migrate/         # Standalone schema migration binary
│   │   │   │   └── main.go
│   │   │   └── server/          # Main API server binary
│   │   │       ├── handler/     # HTTP route controllers
│   │   │       ├── middleware/  # JWT Auth & CORS middleware
│   │   │       ├── repository/  # Database access layer
│   │   │       ├── routes/      # Gin router setup
│   │   │       ├── services/    # Core business domain logic
│   │   │       ├── utils/       # Response helpers and JWT utilities
│   │   │       ├── websockets/  # Concurrent Hub, Client pumps & presence
│   │   │       └── main.go
│   │   └── go.mod
│   │
│   └── frontend/                # Kotlin Multiplatform Frontend
│       ├── androidApp/          # Android platform wrapper (MainActivity)
│       ├── desktopApp/          # Desktop JVM platform wrapper
│       ├── iosApp/              # Xcode iOS project
│       └── shared/              # Shared multiplatform codebase
│           └── src/
│               ├── commonMain/  # Shared Compose UI, MVI ViewModels, Network
│               │   └── kotlin/com/laarasoft/frontend/
│               │       ├── config/        # DI (Koin), Navigation, Theme, Network
│               │       ├── core/          # Utils, SessionManager, MVI Base
│               │       └── features/      # Feature modules (Clean Architecture)
│               │           ├── auth/          # Login, Sign Up
│               │           ├── home/          # Boards listing & Board creation
│               │           ├── kanban/        # Realtime Board, Columns, Cards, Drag-Drop
│               │           ├── organisation/  # Org select / create / invites
│               │           ├── splash/        # Bootstrapping & auth routing
│               │           └── websocket/     # Realtime socket manager & repo
│               ├── androidMain/ # Android platform-specific implementations
│               ├── jvmMain/     # Desktop platform-specific implementations
│               └── nativeMain/  # iOS platform-specific implementations
│
└── packages/                    # Shared Go packages
    ├── config/                  # Environment variable configuration loader
    └── database/                # GORM Postgres connection, models & migrations
        ├── migrate/             # Migration execution logic
        └── models/              # User, Organisation, Board, Section, Issue, Member
```

---

## 📡 Real-Time WebSocket Protocol

When a user opens a board, the frontend establishes a persistent connection to:
```
ws://<host>:8080/api/v1/ws?board_id=<BOARD_UUID>&token=<BEARER_JWT>
```

### Event Specifications

| Event Type | Direction | Description |
|---|---|---|
| `ROOM_STATE` | Server ➔ Client | Initial room snapshot with current online user count and active user list |
| `USER_JOINED` | Server ➔ Client | Broadcast to board members when a new collaborator connects |
| `USER_LEFT` | Server ➔ Client | Broadcast to board members when a collaborator disconnects |
| `SECTION_CREATED` | Server ➔ Client | A new column has been added to the board |
| `SECTION_UPDATED` | Server ➔ Client | A column title or metadata has changed |
| `SECTION_MOVED` | Server ➔ Client | Column order has been reorganized |
| `SECTION_DELETED` | Server ➔ Client | A column has been removed |
| `ISSUE_CREATED` | Server ➔ Client | A new task/card has been created |
| `ISSUE_UPDATED` | Server ➔ Client | Card details (title, description) were edited |
| `ISSUE_MOVED` | Server ➔ Client | Card moved within or across sections |
| `ISSUE_DELETED` | Server ➔ Client | A card was removed from the board |

---

## 🚀 Getting Started

### Prerequisites

1. **Go:** Version 1.24 or newer installed ([golang.org](https://golang.org/)).
2. **PostgreSQL:** Running PostgreSQL database instance (local or hosted e.g. Neon, Supabase, Docker).
3. **Java/JDK:** JDK 17 or JDK 21 (required for Gradle and Android/JVM builds).
4. **Android Studio:** (Optional, for Android development).
5. **Xcode:** (Optional, macOS only, for iOS development).

---

### 1. Backend Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/SahilKarwasra/Trello-App.git
   cd Trello-App
   ```

2. **Configure Environment Variables:**
   Create a `.env` file in the root directory:
   ```env
   DATABASE_URL="postgres://username:password@localhost:5432/trello_db?sslmode=disable"
   JWT_SECRET="your-super-secret-jwt-key"
   PORT=8080
   ```

3. **Run Database Migrations:**
   ```bash
   make migrate
   ```

4. **Start the Go Server:**
   ```bash
   make run
   ```
   The API will be live at `http://localhost:8080/api/v1` and WebSocket at `ws://localhost:8080/api/v1/ws`.

---

### 2. Frontend Setup

Navigate into the frontend project:
```bash
cd apps/frontend
```

#### Run Desktop Application (macOS / Windows / Linux)
```bash
./gradlew :desktopApp:run
```
*(With live hot reload: `./gradlew :desktopApp:hotRun --auto`)*

#### Run Android Application
Open the project in Android Studio or run via command line with an emulator/device connected:
```bash
./gradlew :androidApp:installDebug
```

#### Run iOS Application
Open `apps/frontend/iosApp` in Xcode and click **Run**, or use:
```bash
open iosApp/iosApp.xcworkspace
```

---

## 🛠️ API Reference Summary

### Authentication
- `POST /api/v1/auth/sign-up` — Register a new account (`username`, `email`, `password`)
- `POST /api/v1/auth/sign-in` — Authenticate and receive a JWT Bearer token

### Organizations & Members
- `POST /api/v1/organisation` — Create an organization
- `GET /api/v1/organisation` — List organizations for authenticated user
- `POST /api/v1/invite` — Invite a user to an organization
- `PATCH /api/v1/accept-invite` — Accept an organization invitation

### Boards, Sections & Cards
- `POST /api/v1/board` — Create a new board in an organization
- `GET /api/v1/board?org_id=<id>` — List boards within an organization
- `POST /api/v1/section` — Create a new Kanban section
- `GET /api/v1/section?board_id=<id>` — Fetch all sections & cards for a board
- `PATCH /api/v1/move-section` — Reorder sections
- `DELETE /api/v1/section` — Delete a section
- `POST /api/v1/issue` — Create an issue card
- `PATCH /api/v1/move-issue` — Move an issue between or within sections
- `DELETE /api/v1/issue` — Delete an issue

### WebSockets
- `GET /api/v1/ws?board_id=<id>&token=<jwt>` — Full-duplex real-time board collaboration stream

---

## 🤝 Contributing

Contributions, bug reports, and feature suggestions are welcome!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
