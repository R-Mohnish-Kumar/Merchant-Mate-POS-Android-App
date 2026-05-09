# MerchantMate – Smart POS Android App for Micro-Merchants

MerchantMate is a native Android point-of-sale and sales companion app designed for micro-merchants and small businesses. It helps merchants manage products, build a cart, complete mock checkouts, generate receipts, track transactions, and view useful dashboard insights such as daily revenue, best-selling products, average order value, low-stock items, and revenue comparison.

---

## Project Overview

Many small merchants need a simple and fast way to manage everyday sales without relying on complex POS systems. MerchantMate solves this by providing a lightweight mobile app where a merchant can:

- Register or log in securely
- Manage products and stock
- Add products to a cart
- Complete mock checkout using cash or card
- Generate receipts
- View recent transactions
- Monitor store performance through a dashboard
- Receive smart business insights

The application follows a client-server architecture using a native Android frontend and a Node.js/Express backend connected to Firebase services.

---

## Features

### Authentication

- Firebase Email/Password Authentication
- Secure merchant login and registration
- Firebase ID token sent with each protected API request
- Backend verifies the authenticated merchant using Firebase Admin SDK
- Multi-merchant data isolation using authenticated `merchantId`

### Dashboard

- Personalised greeting using merchant profile owner name
- Today’s revenue
- Total order count
- Average order value
- Best-selling product
- Recent transaction preview
- Smart insights for daily sales performance

### Product Management

- View all products
- Add new products
- Update product details
- Delete products
- Track product price, stock, and category
- Stock is automatically reduced after checkout

### Cart and Checkout

- Add products to cart
- Increase quantity while respecting available stock
- View cart total
- Choose payment method: `CASH` or `CARD`
- Complete mock checkout
- Checkout creates a transaction record
- Cart clears after successful checkout

### Receipts and Transactions

- View completed transactions
- Receipt ID generated for every checkout
- Display total amount, payment method, status, and date
- Receipt-style transaction history screen

### Smart Insights

- Top-selling product of the day
- Low-stock product alerts
- Slow-moving product identification
- Average order value
- Revenue comparison against previous day
- Suggested actions for the merchant

### Profile

- Merchant profile API integration
- Owner name displayed in dashboard greeting
- Merchant email and business details support

---

## Tech Stack

### Android Frontend

- Kotlin
- XML Layouts
- MVVM Architecture
- ViewModel
- LiveData
- RecyclerView
- ViewBinding
- Retrofit
- OkHttp Interceptor
- Material Components
- Firebase Authentication

### Backend

- Node.js
- Express.js
- Firebase Admin SDK
- Firestore
- REST API Architecture
- Middleware-based authentication

### Database and Cloud Services

- Firebase Authentication
- Cloud Firestore

---

## Architecture

```text
Android App
   |
   | Retrofit API calls
   |
Node.js / Express Backend
   |
   | Firebase Admin SDK
   |
Firestore Database
```

Authenticated flow:

```text
Firebase Auth Login
   |
Android receives Firebase ID Token
   |
Token sent as Authorization: Bearer <token>
   |
Backend verifies token using Firebase Admin SDK
   |
Backend uses uid as merchantId
   |
Data stored under merchants/{merchantId}/...
```

---

## Firestore Data Structure

```text
merchants
 └── {merchantId}
      ├── profile
      ├── products
      │    └── {productId}
      └── transactions
           └── {transactionId}
```

This structure ensures every merchant only accesses their own products, transactions, dashboard data, and insights.

---

## Main API Endpoints

### Health Check

```http
GET /
```

### Products

```http
GET /api/products
POST /api/products
PUT /api/products/:id
DELETE /api/products/:id
```

### Checkout

```http
POST /api/checkout
```

### Transactions

```http
GET /api/transactions
```

### Dashboard

```http
GET /api/dashboard/summary
```

### Insights

```http
GET /api/insights/today
```

### Profile

```http
GET /api/profile
PUT /api/profile
```

Protected endpoints require:

```http
Authorization: Bearer <Firebase_ID_Token>
```

---

## Android Project Structure

```text
com.example.merchantmate
│
├── ui
│   ├── SplashActivity.kt
│   ├── LoginActivity.kt
│   ├── MainActivity.kt
│   ├── CartActivity.kt
│   ├── ReceiptsActivity.kt
│   └── ProfileActivity.kt
│
├── adapter
│   ├── ProductAdapter.kt
│   ├── CartAdapter.kt
│   └── ReceiptAdapter.kt
│
├── model
│   ├── Product.kt
│   ├── ProductRequest.kt
│   ├── CheckoutRequest.kt
│   ├── Transaction.kt
│   ├── DashboardSummary.kt
│   ├── TodayInsights.kt
│   ├── MerchantProfile.kt
│   └── ApiResponse.kt
│
├── network
│   ├── ApiService.kt
│   ├── ApiClient.kt
│   └── AuthInterceptor.kt
│
├── repository
│   └── ProductRepository.kt
│
├── viewmodel
│   ├── ProductViewModel.kt
│   ├── ProductViewModelFactory.kt
│   └── UiState.kt
```

Note: The package structure may vary slightly depending on the final project organisation.

---

## Key Screens

### Login Screen

A polished login and registration screen with Firebase Authentication.

### Dashboard Screen

Displays today’s store performance, revenue, orders, average order value, best seller, recent transaction, and smart insights.

### Products Screen

Allows the merchant to add, view, update, and delete products.

### Checkout Screen

Allows the merchant to add products to cart and complete checkout using cash or card.

### Receipts Screen

Displays completed transactions and generated receipt IDs.

### Profile Screen

Displays merchant information and supports profile-based dashboard personalisation.

---

## Setup Instructions

### Prerequisites

Install:

- Android Studio
- Node.js
- Firebase project
- Firebase Authentication enabled
- Cloud Firestore enabled

---

## Backend Setup

Clone the backend project or open the backend folder.

Install dependencies:

```bash
npm install
```

Create a `.env` file:

```env
PORT=5000
DEV_AUTH_BYPASS=false
DEV_MERCHANT_ID=test-merchant-001
```

Add your Firebase Admin SDK service account configuration as required by your backend setup.

Run the backend:

```bash
npm start
```

For development:

```bash
npm run dev
```

The backend should run on:

```text
http://localhost:5000
```

For Android Emulator, the app uses:

```text
http://10.0.2.2:5000/
```

---

## Android Setup

Open the Android project in Android Studio.

Add your Firebase configuration file:

```text
app/google-services.json
```

Make sure Firebase Auth is enabled in Firebase Console.

Check the Retrofit base URL:

```kotlin
private const val BASE_URL = "http://10.0.2.2:5000/"
```

For local HTTP testing, make sure the manifest includes:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

And inside the application tag:

```xml
android:usesCleartextTraffic="true"
```

Sync Gradle and run the app.

---

## Important Android Dependencies

```kotlin
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("androidx.cardview:cardview:1.0.0")
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
implementation("com.google.android.material:material:1.12.0")
implementation("androidx.appcompat:appcompat:1.7.0")
implementation("androidx.core:core-splashscreen:1.0.1")
implementation(platform("com.google.firebase:firebase-bom:latest_version"))
implementation("com.google.firebase:firebase-auth")
```

---

## Example Checkout Flow

```text
1. Merchant logs in
2. Dashboard loads merchant profile and daily summary
3. Merchant adds products
4. Merchant adds products to cart
5. Merchant selects payment method: CASH or CARD
6. Checkout request is sent to backend
7. Backend creates a transaction
8. Product stock is reduced
9. Receipt is generated
10. Dashboard and insights refresh automatically
```

---

## Sample API Request

### Checkout Request

```json
{
  "items": [
    {
      "productId": "abc123",
      "name": "Cranberry Juice",
      "price": 4.20,
      "quantity": 2
    }
  ],
  "paymentMethod": "CARD"
}
```

---

## Sample Dashboard Response

```json
{
  "success": true,
  "data": {
    "todayRevenue": 331.20,
    "transactionCount": 9,
    "averageOrderValue": 36.80,
    "bestSellingProduct": {
      "productId": "product123",
      "name": "Cranberry Juice",
      "quantitySold": 19,
      "revenue": 79.80
    }
  }
}
```

---

## Challenges Solved

During development, several practical issues were handled:

- Firebase ID token authentication with Retrofit
- Multi-merchant Firestore data isolation
- Firestore timestamp parsing in Android
- Protected backend route testing
- Checkout validation for payment method
- Stock reduction after checkout
- Dashboard refresh after transaction creation
- Android splash screen and app theme issues
- Material Components theme compatibility
- Clean XML-based UI polish

---

## Future Improvements

- Real payment gateway integration
- Barcode scanning for products
- Product image upload
- Receipt sharing as PDF
- Offline mode with local caching
- Sales analytics by week/month
- Export transactions as CSV
- Push notifications for low stock
- Role-based access for staff accounts
- Dark mode support

---

## Project Pitch

MerchantMate is a native Android smart sales companion for micro-merchants. It helps merchants manage products, complete mock checkouts, generate receipts, and view daily business insights. I built the Android app using Kotlin with XML layouts, MVVM, Retrofit, RecyclerView, and Firebase Authentication. The backend uses Node.js, Express, Firebase Admin SDK, and Firestore to provide secure multi-merchant data isolation.

---

## Author

**Mohnishkumar Rajkumar**  
MSc Artificial Intelligence  
Birmingham City University

GitHub: [R-Mohnish-Kumar](https://github.com/R-Mohnish-Kumar)

---

## License

This project is developed for academic and portfolio purposes.
