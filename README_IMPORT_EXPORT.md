# 📦 Importing and Exporting Products via CSV

This guide explains how to **import** and **export** product data in CSV format using the Pharmacy Management App.

---

## 🚀 Exporting Products (CSV)

1. **Open the app** and log in as an admin.
2. Open the **side menu** (drawer).
3. Click **Export Products**.
4. When prompted, choose **CSV**.
5. The app will generate a CSV file with all your products and let you share or save it.

---

## 📥 Importing Products (CSV)

1. **Download the CSV Template** (see below) or export your current products first.
2. Open the template in **Excel**, **Google Sheets**, or any spreadsheet editor.
3. **Fill in your product data** under each column:
   - `name`, `description`, `price`, `stock`, `expiryDate`, `manufacturer`, `imageUri`, `barcode`, `category`
4. **Save the file as CSV** (Comma Separated Values).
5. In the app, open the **side menu** (drawer).
6. Click **Import Products**.
7. When prompted, choose **CSV**.
8. Select your CSV file and confirm import.

---

## 🏷️ CSV Template Example

```
name,description,price,stock,expiryDate,manufacturer,imageUri,barcode,category
Paracetamol,500mg Tablets,2.99,50,2026-12-31,PharmaCorp,,1234567890123,Pain Relief
Ibuprofen,200mg Tablets,3.49,30,2025-11-30,Medico Ltd,,9876543210987,Pain Relief
```

- **Note:** The first row must contain the column headers.
- Leave `imageUri` blank unless you have a specific image URI.

---

## 🛠️ How to Create a CSV File

### Option 1: Using Excel
1. Open Excel and create a new workbook.
2. Enter the column headers in the first row.
3. Fill in your product data below each header.
4. Go to **File > Save As** and choose **CSV (Comma delimited) (*.csv)**.

### Option 2: Using Google Sheets
1. Go to [Google Sheets](https://sheets.google.com) and start a new sheet.
2. Enter the headers and product data.
3. Go to **File > Download > Comma-separated values (.csv, current sheet)**.

### Option 3: Using Notepad
1. Open Notepad or any plain text editor.
2. Type the headers and product data, separated by commas.
3. Save the file with a `.csv` extension.

---

## ℹ️ Tips
- Do **not** use commas inside fields unless you enclose the value in double quotes.
- `expiryDate` format: `YYYY-MM-DD` (e.g., 2026-12-31)
- Each product should have a unique `barcode`.
- If a barcode already exists, the product will be **updated** instead of duplicated.

---

## ❓ Need Help?
If you have any trouble importing or exporting CSV files, please refer to this guide or contact your system administrator.
