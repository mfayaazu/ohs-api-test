package com.integration.util;

import static order.Order.OrderStatus;
import static order.Order.OrderStatus.*;

public class OrderStatusUtil {
    public static OrderStatus mapStringToOrderStatus(String status) {
        switch (status) {
            case "0" -> {
                return CREATED;
            }
            case "1" -> {
                return SHIPPED;
            }
            case "2" -> {
                return DELIVERED;
            }
            default -> throw new IllegalArgumentException("Unknown order status: " + status);
        }
    }
}
