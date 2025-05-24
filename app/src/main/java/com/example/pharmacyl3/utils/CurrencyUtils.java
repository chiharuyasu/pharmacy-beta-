package com.example.pharmacyl3.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    // Exchange rate: 1 USD = 134.50 DZD (you can update this rate as needed)
    private static final double USD_TO_DZD_RATE = 134.50;
    
    /**
     * Format a price in DZD (Algerian Dinar)
     * @param priceInUsd The price in USD
     * @return Formatted price string in DZD (e.g., "1,345.00 DA")
     */
    public static String formatPrice(double priceInUsd) {
        double priceInDzd = priceInUsd * USD_TO_DZD_RATE;
        NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return String.format("%s DA", format.format(priceInDzd));
    }
    
    /**
     * Format a price with quantity in DZD (Algerian Dinar)
     * @param priceInUsd The price per unit in USD
     * @param quantity The quantity
     * @return Formatted price string with quantity (e.g., "1,345.00 DA x 2 = 2,690.00 DA")
     */
    public static String formatPriceWithQuantity(double priceInUsd, int quantity) {
        double totalPriceInDzd = priceInUsd * USD_TO_DZD_RATE * quantity;
        double unitPriceInDzd = priceInUsd * USD_TO_DZD_RATE;
        NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        
        return String.format("%s x %d = %s", 
            format.format(unitPriceInDzd), 
            quantity, 
            format.format(totalPriceInDzd));
    }
    
    /**
     * Convert USD to DZD
     * @param usdAmount Amount in USD
     * @return Amount in DZD
     */
    public static double usdToDzd(double usdAmount) {
        return usdAmount * USD_TO_DZD_RATE;
    }
}
