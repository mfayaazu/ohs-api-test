package com.integration.util;

public class OrderStatusUtil {
    public static int mapStringToOrderStatus(String status) {
        switch (status) {
            case "0" -> {
                return 0;
            }
            case "1" -> {
                return 1;
            }
            case "2" -> {
                return 2;
            }
            default -> throw new IllegalArgumentException("Unknown order status: " + status);
        }
    }
}
