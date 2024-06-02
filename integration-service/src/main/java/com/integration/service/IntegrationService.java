package com.integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.integration.grpc.OrderServiceClient;
import com.integration.grpc.ProductServiceClient;
import com.integration.grpc.UserServiceClient;
import com.integration.model.OrderCsv;
import com.integration.model.ProcessedOrder;
import com.integration.util.OrderStatusUtil;
import io.grpc.StatusRuntimeException;
import order.Order;
import org.springframework.stereotype.Service;
import product.Product;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.google.protobuf.StringValue.of;
import static order.Order.*;
import static order.Order.OrderStatus.*;
import static user.User.*;

@Service
public class IntegrationService {

    private final CsvReaderService csvReaderService;

    private final UserServiceClient userServiceClient;

    private final OrderServiceClient orderServiceClient;

    private final ProductServiceClient productServiceClient;

    private static final String OUTPUT_FILE_PATH = "/Users/fayaazuddinalimohammad/Downloads/ohs-api-test/integration-service/src/main/resources/output/processed-orders.json";
    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter OUTPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public IntegrationService(CsvReaderService csvReaderService,
                              UserServiceClient userServiceClient,
                              OrderServiceClient orderServiceClient, ProductServiceClient productServiceClient) {
        this.csvReaderService = csvReaderService;
        this.userServiceClient = userServiceClient;
        this.orderServiceClient = orderServiceClient;
        this.productServiceClient = productServiceClient;

    }


    public void processCsvFile(String filePath) throws IOException {
        List<OrderCsv> orders = csvReaderService.readOrders(filePath);
        List<ProcessedOrder> processedOrders = new ArrayList<>();
        for (OrderCsv order : orders) {

            String userPid;

            // Create user
            CreateUserRequest userRequest = CreateUserRequest.newBuilder()
                    .setFullName(of(order.getFirstName() + " " + order.getLastName()))
                    .setEmail(order.getEmail())
                    .setAddress(ShippingAddress
                            .newBuilder()
                            .setAddress(of(order.getShippingAddress()))
                            .setCountry(of(order.getCountry()))
                            .build())
                    .addPaymentMethods(PaymentMethod.newBuilder()
                            .setCreditCardNumber(of(order.getCreditCardNumber()))
                            .setCreditCardType(of(order.getCreditCardType()))
                            .build())
                    .setPassword(of(" "))
                    .build();
            try {
                UserResponse userResponse = userServiceClient.createUser(userRequest);
                userPid = userResponse.getPid();
            } catch (StatusRuntimeException e) {
                System.err.println(e.getMessage());
                continue;
            }

            // Create order
            Product.ProductResponse productResponse = productServiceClient.getProductByPid(of(order.getProductPid()));
            Order.Product productDetails = Order.Product.newBuilder()
                    .setPid(productResponse.getPid())
                    .setPricePerUnit(productResponse.getPricePerUnit())
                    .setQuantity(Integer.parseInt(order.getQuantity()))
                    .build();
            String dateCreatedStr = order.getDateCreated();
            LocalDate dateCreated = ZonedDateTime.parse(dateCreatedStr, INPUT_DATE_FORMATTER).toLocalDate();
            String formattedDateCreated = dateCreated.format(OUTPUT_DATE_FORMATTER);
            var orderStatus = order.getOrderStatus();

            CreateOrderRequest orderRequest = CreateOrderRequest.newBuilder()
                    .addProducts(productDetails)
                    .setUserPid(userPid)
                    .setDateCreated(of(formattedDateCreated))
                    .setStatusValue(OrderStatusUtil.mapStringToOrderStatus(orderStatus))
                    .setPricePerUnit(productResponse.getPricePerUnit())
                    .setQuantity(Integer.parseInt(order.getQuantity()))
                    .setDateDelivered(of(ZonedDateTime
                            .now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                    .build();

            OrderResponse orderResponse = orderServiceClient.createOrder(orderRequest);
            String orderPid = orderResponse.getPid();
            String supplierPid = order.getSupplierPid();

            ProcessedOrder processedOrder = new ProcessedOrder(userPid, orderPid, supplierPid);
            processedOrders.add(processedOrder);

            writeProcessedOrdersToJson(processedOrders);

            System.out.println("Processed Order - UserPid: " + userPid + ", OrderPid: " + /*orderResponse.getPid() +*/ ", SupplierPid: " + order.getSupplierPid());
        }
    }

    private void writeProcessedOrdersToJson(List<ProcessedOrder> processedOrders) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

        try {
            writer.writeValue(new File(OUTPUT_FILE_PATH), processedOrders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
