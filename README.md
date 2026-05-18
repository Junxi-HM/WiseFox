# 🦊 WiseFox — Frontend (Android)

> **WiseFox** is a personal finance management application that allows users to track income and expenses, organise their money into shared ledgers, and collaborate with other users.

🔗 **Backend (Spring Boot):** [https://github.com/Joan735/WiseFox_Backend](https://github.com/Joan735/WiseFox_Backend)

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Architecture](#project-architecture)
- [Screens & Navigation](#screens--navigation)
- [Backend Communication](#backend-communication)
- [Authentication](#authentication)
- [Data Models](#data-models)
- [Setup & Running](#setup--running)

---

## Overview

WiseFox Android is the mobile client that consumes the WiseFox REST API. It is built in **Kotlin** with **Jetpack Compose** and follows an **MVVM** architecture. Users can manage their personal finances on the go: create and share ledgers, record transactions, and view summaries — all with a clean, modern UI.

---

## Tech Stack

| Technology | Version |
|---|---|
| Kotlin | 2.0.21 |
| Android Gradle Plugin | 8.13.2 |
| Jetpack Compose BOM | 2024.09.00 |
| Material 3 | 1.4.0 |
| Navigation Compose | 2.7.7 |
| Retrofit | 2.9.0 |
| OkHttp Logging Interceptor | 4.12.0 |
| Gson Converter | 2.9.0 |
| Credential Manager | 1.3.0 |
| Google Identity (Google Sign-In) | 1.1.1 |
| Coroutines | 1.7.3 |
| ViewModel Compose | 2.7.0 |
| min SDK | 28 (Android 9) |
| target SDK | 36 |

---

## Project Architecture

```
app/src/main/java/com/example/wisefox/
├── MainActivity.kt          # Entry point; hosts the NavHost and locale management
├── navigation/
│   ├── Screen.kt            # Sealed class with all route definitions
│   └── NavGraph.kt          # Navigation graph and screen wiring
├── screens/
│   ├── common/
│   │   └── Layout.kt        # WiseFoxLayout shell (gradient bg, card, bottom nav)
│   ├── SplashScreen.kt
│   ├── LoginScreen.kt
│   ├── GoogleRegisterScreen.kt
│   ├── HomeScreen.kt
│   ├── TransactionsScreen.kt
│   ├── AIScreen.kt
│   ├── ProfileScreen.kt
│   ├── EditProfileScreen.kt
│   ├── SharedLedgersScreen.kt
│   └── LedgerDetailScreen.kt
├── viewmodels/
│   ├── LoginViewModel.kt
│   ├── HomeViewModel.kt
│   └── ProfileViewModel.kt
├── network/
│   ├── RetrofitClient.kt    # Singleton Retrofit instance with JWT interceptor
│   ├── AuthApiService.kt
│   ├── UserApiService.kt
│   ├── LedgerApiService.kt
│   ├── TransactionApiService.kt
│   └── UserLedgerApiService.kt
├── model/                   # Data classes mirroring backend DTOs
└── utils/
    ├── SessionManager.kt    # JWT token storage
    └── LocaleHelper.kt      # Runtime language switching
```

---

## Screens & Navigation

| Screen | Route | Description |
|---|---|---|
| Splash | `splash` | Animated launch screen with logo and spinner |
| Login | `login` | Email/password login and Google Sign-In |
| Google Register | `google_register/{googleToken}/{email}` | Complete profile after Google sign-in |
| Home | `home` | Dashboard with ledger cards (totals per ledger) |
| Transactions | `transactions` | Transaction list with filters |
| AI | `ai` | AI-powered financial assistant |
| Profile | `profile` | User profile and settings |
| Edit Profile | `edit_profile` | Update name, username, email, photo |
| Shared Ledgers | `shared_ledgers` | Ledgers shared with other users |
| Ledger Detail | `ledger_detail/{ledgerId}` | Transactions and members for a single ledger |

All main screens (Home, Transactions, AI, Profile) are wrapped in `WiseFoxLayout`, which provides the warm yellow gradient background, the rounded orange content card, and the bottom navigation bar.

---

## Backend Communication

All HTTP calls are made via **Retrofit** with a shared `OkHttpClient`:

- A **JWT interceptor** automatically attaches the `Authorization: Bearer <token>` header to every protected request.
- Public auth routes (login, register, Google flow, password reset) bypass the interceptor and send no token.
- A **logging interceptor** logs full request/response bodies in debug builds.
- Connect and read timeouts are set to **15 seconds**.
- `LocalDate` fields are deserialised with a custom **Gson** type adapter.

```
Base URL: http://<server-ip>:8080/
```

> Update `BASE_URL` in `RetrofitClient.kt` to point to your backend instance.

---

## Authentication

The app supports two login methods:

**Standard (email + password)**
1. User submits credentials → `POST /api/auth/login`
2. Backend returns a JWT token which is stored via `SessionManager`.

**Google Sign-In**
1. Android Credential Manager obtains a Google ID Token.
2. Token is sent to `POST /api/auth/google`.
3. If the account already exists → JWT returned immediately.
4. If new → a 6-digit code is emailed; user verifies via `POST /api/auth/verify-code`.
5. User completes registration via `POST /api/auth/register/google`.

**Password Reset**
1. `POST /api/auth/forgot-password` sends a reset code to the user's email.
2. `POST /api/auth/verify-reset-code` validates the code.
3. `POST /api/auth/reset-password` sets the new password.

The JWT is persisted in `SessionManager` and included in all subsequent requests until the user logs out.

---

## Data Models

### `UserResponse`

| Field | Type | Description |
|---|---|---|
| `id` | Long | User ID |
| `name` | String | First name |
| `surname` | String | Last name |
| `username` | String | Username |
| `email` | String | Email address |
| `role` | String | `USER` or `PREMIUM` |
| `hasProfilePicture` | Boolean | Whether a profile picture is set |

### `LedgerResponse`

| Field | Type | Description |
|---|---|---|
| `id` | Long | Ledger ID |
| `name` | String | Ledger name |
| `currency` | String | Currency code |
| `description` | String? | Optional description |
| `ownerId` | Long? | Owner's user ID |
| `ownerUsername` | String? | Owner's username |
| `memberCount` | Int | Number of shared members |

### `TransactionResponse`

| Field | Type | Description |
|---|---|---|
| `id` | Long? | Transaction ID |
| `amount` | Double? | Amount |
| `type` | Enum | `INCOME` \| `EXPENSE` |
| `category` | Enum | `FOOD`, `TRANSPORT`, `RENT`, etc. |
| `date` | LocalDate? | Transaction date |
| `note` | String? | Optional note |
| `ledgerId` | Long? | Parent ledger ID |
| `ledgerName` | String? | Parent ledger name |

### `LedgerUiModel`

A UI-layer model computed from a `LedgerResponse` and its transactions, adding `totalExpenses` and `totalEarnings` for display in the Home screen cards.

---

## Setup & Running

### Prerequisites

- Android Studio Hedgehog or later
- Android device or emulator running API 28+
- A running instance of the [WiseFox Backend](https://github.com/Joan735/WiseFox_Backend)

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/Junxi-HM/WiseFox.git
cd WiseFox
```

1. Open the project in **Android Studio**.
2. In `app/src/main/java/com/example/wisefox/network/RetrofitClient.kt`, update `BASE_URL` to point to your backend:
   ```kotlin
   private const val BASE_URL = "http://<your-server-ip>:8080/"
   ```
3. Sync Gradle and **Run** the app on your device or emulator.

> For a physical device on the same Wi-Fi network as the backend, use the machine's local IP address. For the Android emulator, use `10.0.2.2` to reach `localhost` on the host machine.

---

*WiseFox Android — Track Smart, Live Wisely. 🦊*
