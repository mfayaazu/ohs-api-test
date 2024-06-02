package com.integration.util;

public class PriceUtil {

    /**
     * Remove currency symbol and parse the price string to a float.
     * @param priceStr the price string containing the currency symbol
     * @return the parsed float value
     * @throws NumberFormatException if the string cannot be parsed to a float
     */
    public static float parsePrice(String priceStr) {
        if (priceStr != null && !priceStr.isEmpty()) {
            // Remove non-numeric characters except the decimal point
            String cleanedPriceStr = priceStr.replaceAll("[^\\d.]", "");
            return Float.parseFloat(cleanedPriceStr);
        } else {
            throw new NumberFormatException("Price string is null or empty");
        }
    }
}
