# Pharmacy Beta

A modern Android application for pharmacy management, supporting both customers and pharmacists/admins. The app enables product browsing, ordering, customer profile management, and admin inventory control, all using a local SQLite database.

---

## Project Structure

```
pharmacy-beta-
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/pharmacyl3/
│   │       │   ├── AdminActivity.java
│   │       │   ├── AdminDashboardActivity.java
│   │       │   ├── AdminProductAdapter.java
│   │       │   ├── BarcodeScannerActivity.java
│   │       │   ├── CartActivity.java
│   │       │   ├── CartAdapter.java
│   │       │   ├── Customer.java
│   │       │   ├── CustomerActivity.java
│   │       │   ├── CustomerEditProfileActivity.java
│   │       │   ├── CustomerSignUpActivity.java
│   │       │   ├── DBHelper.java
│   │       │   ├── EditProfileActivity.java
│   │       │   ├── LoginActivity.java
│   │       │   ├── Order.java
│   │       │   ├── OrderHistoryActivity.java
│   │       │   ├── OrderHistoryAdapter.java
│   │       │   ├── Product.java
│   │       │   ├── ProductDetailActivity.java
│   │       │   ├── ProductRecyclerAdapter.java
│   │       │   └── ProfileManager.java
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   ├── values/
│   │       │   └── ...
│   │       └── AndroidManifest.xml
│   └── ...
├── build.gradle.kts
├── settings.gradle.kts
└── ...
```

---

## Core Features

### Customer Side
- **Sign Up / Login:** Customers can create an account and log in securely.
- **Profile Management:**
  - Dedicated profile editing screen for customers (`CustomerEditProfileActivity`).
  - Fields: Name, Email, Phone, Profile Photo (editable after signup).
  - Profile data is loaded and saved securely in SQLite.
- **Product Browsing:** View available products, search, and filter.
- **Cart & Orders:** Add items to cart, place orders, and view order history.

### Pharmacist/Admin Side
- **Admin Dashboard:** Manage products, view orders, scan barcodes, and update inventory.
- **Product Management:** Add, edit, and delete products with rich metadata.
- **Order Management:** View and manage all customer orders.

---

## Database
- **SQLite** is used for all persistent data.
- **DBHelper.java:** Centralized helper for all database operations (customers, products, orders, cart, etc).
- **Customers Table:** Stores both customer and pharmacist data (with relevant fields for each).

---

## Design Decisions
- **Separation of Concerns:**
  - Separate activities for customer and pharmacist/admin flows.
  - Dedicated customer profile editing screen (no pharmacist fields for customers).
- **User Experience:**
  - Profile editing pre-fills all available data.
  - Profile photo can be updated from the edit profile screen.
- **Security:**
  - Passwords stored securely in SQLite (consider hashing in production).
  - Sensitive data handled with care.

---

## Getting Started
1. Open the project in Android Studio.
2. Build and run on an emulator or device.
3. Sign up as a customer or log in as an admin.

---

## File Highlights
- `CustomerActivity.java` — Main customer dashboard, navigation, and profile drawer.
- `CustomerEditProfileActivity.java` — Customer profile editing.
- `DBHelper.java` — All database logic.
- `AdminActivity.java` — Main admin interface.
- `ProductRecyclerAdapter.java` — Product list UI logic.

---

## Future Enhancements
- Add input validation for email and phone formats.
- Implement password hashing for extra security.
- Add remote/cloud sync support.
- Enhance UI/UX for smoother navigation.

---

## License
This project is for educational/demo purposes. Add your license as needed.
