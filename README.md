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
  - **Profile photo selection:** Customers can pick a photo from their device; the app copies it to internal storage for reliable display.
  - **Circular profile photo:** Profile photos are displayed as circles in both the drawer header and the edit profile screen.
  - **Live header update:** After editing and saving the profile, the drawer header updates immediately with the new name, phone, and photo.
  - Profile data is loaded and saved securely in SQLite.
- **Product Browsing:** View available products, search, and filter.
    - **NEW:** Customers can search products by name and filter by category, with both filters working together in real time (just like the admin interface).
- **Cart & Orders:** Add items to cart, place orders, and view order history.

### Pharmacist/Admin Side
- **Admin Dashboard:** Manage products, view orders, scan barcodes, and update inventory.
- **Product Management:** Add, edit, and delete products with rich metadata.
    - **NEW:** Admins can filter products by both category and search text at the same time for efficient management.
- **Order Management:** View and manage all customer orders.
- **Admin profile photo:** Admins can also set and update a circular profile photo in the header.

---

## Database
- **SQLite** is used for all persistent data.
- **DBHelper.java:** Centralized helper for all database operations (customers, products, orders, cart, etc).
- **Customers Table:** Stores both customer and pharmacist data (with relevant fields for each).
- **Profile photo URIs:** Profile photos are saved as file URIs in the database for persistence and reliability.

---

## Design Decisions & Improvements
- **Separation of Concerns:**
  - Separate activities for customer and pharmacist/admin flows.
  - Dedicated customer profile editing screen (no pharmacist fields for customers).
- **User Experience:**
  - Profile editing pre-fills all available data.
  - Profile photo can be updated from the edit profile screen and is displayed as a circle.
  - Drawer header updates instantly after profile changes.
- **Security:**
  - Passwords stored securely in SQLite (consider hashing in production).
  - Profile photos are copied to internal storage to avoid issues with temporary URIs and permissions.
- **Error Handling:**
  - Graceful fallback to placeholder image if photo cannot be loaded.

---

## Recent Feature Additions & Fixes
- Persistent, circular profile photos for both customers and admins.
- Live drawer header update after editing profile (name, phone, photo).
- Improved image selection and storage logic for reliability.
- Consistent UI for profile photo display in edit and header screens.
- Bug fixes for profile photo not showing or causing crashes after editing.
- **Dedicated Admin Profile Editing**: Added a new `AdminEditProfileActivity` for admins/pharmacists with fields for name, phone, license number, pharmacy name, address, years of experience, email, and profile photo. Admin profile data is stored in `SharedPreferences` via `ProfileManager`.
- **Admin Profile Photo in Header**: Admins can set/update their profile photo. The navigation drawer header now reliably displays the updated photo (supports both content and file URIs).
- **Combined Filtering for Admin**: The admin product list now supports combined filtering by both category and search text for fast and precise inventory management.
- **Combined Filtering for Customer**: Customers can now search for products by name and filter by category simultaneously, making product discovery easier and more intuitive.
- **Clear All Notifications:** Admins can now clear all notifications at once from the notifications screen using a dedicated button.
- **Clear Order History:** Customers can clear their entire order history with a single tap from the order history screen.

---

## Inventory & Notification System (2025)
- **Low Stock Notification:** Admins are alerted via a Snackbar and persistent notification when a product's stock falls below a threshold.
- **Expiring Soon Notification:** Admins receive a Snackbar and notification when products are nearing expiry (default: within 30 days).
- **Expired Product Notification:** Admins are notified via Snackbar and notification if any product has expired.
- **Order Placed Notification:** When a customer places an order, admins receive a Snackbar alert (if active) and a persistent notification.
- **Notifications Center:** Admins can view all alerts (low stock, expiring, expired, order placed) in a dedicated notifications activity.
- **Snackbar 'VIEW' Action:** All Snackbars have a 'VIEW' button that opens the notifications screen for quick management.
- **Database Notifications Table:** Added a notifications table to SQLite for tracking all admin alerts.
- **Cart Quantity Picker:** Customers can select quantity when adding to cart; cart badge reflects total quantity.
- **Bug Fixes:** Improved reliability of notification triggers, cart badge, and order processing.

---

## Cart and Stock Logic Improvements (2025-04)
- **Cart Logic Overhaul:**
  - Adding items to the cart does NOT reduce product stock immediately. Stock is only reduced after a successful checkout.
  - The cart allows you to add up to the full available stock (as shown in the product list) regardless of previous cart actions.
  - Undoing an add-to-cart operation only updates the cart, not the product stock.
- **Checkout Process:**
  - At checkout, the app checks the latest stock in the database for each product in your cart.
  - If enough stock is available, the order is placed and product stock is reduced in the database.
  - If not enough stock is available (e.g., another user bought the product first), a clear message is shown and the order is not placed for that product.
- **Stock Restoration:**
  - When you remove an item from the cart, the product stock is restored in the database.
  - The product list UI is refreshed immediately after returning from the cart, so you always see the correct available stock.
- **Consistency:**
  - Product list and cart are always in sync with the database after cart changes or checkout.
  - No more issues with premature stock reduction or incorrect stock warnings when adding to cart or checking out.

---

## Getting Started

1. Open the project in Android Studio.
2. Build and run on an emulator or device (no special configuration needed).
3. Use the app as a customer or admin to explore all features.

---

## Credits
- Developed by Chiharuyasu
- For support or feedback, open an issue or contact the author.

---

## License
This project is for educational/demo purposes. Add your license as needed.

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
