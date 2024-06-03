package com.integration.model;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class OrderCsv {
    @CsvBindByName
    private String id;

    @CsvBindByName(column = "first_name")
    private String firstName;

    @CsvBindByName(column = "last_name")
    private String lastName;

    @CsvBindByName
    private String email;

    @CsvBindByName(column = "supplier_pid")
    private String supplierPid;

    @CsvBindByName(column = "credit_card_number")
    private String creditCardNumber;

    @CsvBindByName(column = "credit_card_type")
    private String creditCardType;

    @CsvBindByName(column = "order_id")
    private String orderId;

    @CsvBindByName(column = "product_pid")
    private String productPid;

    @CsvBindByName(column = "shipping_address")
    private String shippingAddress;

    @CsvBindByName
    private String country;

    @CsvBindByName(column = "date_created")
    private String dateCreated;

    @CsvBindByName
    private String quantity;

    @CsvBindByName(column = "full_name")
    private String fullName;

    @CsvBindByName(column = "order_status")
    private String orderStatus;
}