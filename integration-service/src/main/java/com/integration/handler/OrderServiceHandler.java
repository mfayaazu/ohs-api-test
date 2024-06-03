package com.integration.handler;

import com.google.protobuf.Int64Value;
import com.integration.grpc.OrderServiceClient;
import com.integration.grpc.ProductServiceClient;
import com.integration.model.ExtractedOrderIdFromPageableOrderResponse;
import com.integration.model.OrderCsv;
import com.integration.model.Result;
import com.integration.util.OrderStatusUtil;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import order.Order;
import org.springframework.stereotype.Service;
import product.Product;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.google.protobuf.StringValue.of;
import static order.Order.*;

@Service
@Slf4j
public class OrderServiceHandler {
    private final OrderServiceClient orderServiceClient;
    private final ProductServiceClient productServiceClient;

    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter OUTPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public OrderServiceHandler(OrderServiceClient orderServiceClient, ProductServiceClient productServiceClient) {
        this.orderServiceClient = orderServiceClient;
        this.productServiceClient = productServiceClient;
    }

    /**
     * Calls the OrderService to create an order and retrieve the order and supplier IDs.
     *
     * <p>This method creates an order request from the given {@link OrderCsv} and user Id, sends the
     * request to the OrderService, and then retrieves all orders using a pageable request. It extracts
     * the order ID and supplier ID from the pageable response based on the given order and user Id.</p>
     *
     * @param order   the order data from the CSV
     * @param userPid the user ID
     * @return a {@link Result} object containing the supplier ID and order ID
     */
    public Optional<Result> callOrderService(OrderCsv order, String userPid) {
        createOrder(order, userPid);
        return getAllOrders(order, userPid);
    }

    private Optional<Result> getAllOrders(OrderCsv order, String userPid) {
        PageableRequest requestPageable = PageableRequest.newBuilder().setNumberPerPage(Int64Value.of(100)).build();
        log.debug("Created PageableRequest: {}", requestPageable);

        PageableResponse pageableResponse;
        try {
            pageableResponse = orderServiceClient.getALlOrders(requestPageable);
            log.info("Successfully fetched pageable orders from OrderService for userPid: {}", userPid);
        } catch (StatusRuntimeException e) {
            log.error("Failed to fetch pageable orders from OrderService for userPid: {}. Error: {}", userPid, e.getMessage());
            return Optional.empty();
        }

        ExtractedOrderIdFromPageableOrderResponse extractOrderIdFromPageableOrderResponse = getExtractedOrderIdFromPageableOrderResponse(order, userPid, pageableResponse);
        log.debug("Extracted Order ID from pageable response: {}", extractOrderIdFromPageableOrderResponse);

        if (extractOrderIdFromPageableOrderResponse.orderId() == null) {
            log.error("Order ID not found for userPid: {}", userPid);
            return Optional.empty();
        }

        Result result = new Result(extractOrderIdFromPageableOrderResponse.supplierPid(), extractOrderIdFromPageableOrderResponse.orderId());
        log.info("Successfully processed order for userPid: {}. Result: {}", userPid, result);

        return Optional.of(result);
    }

    private void createOrder(OrderCsv order, String userPid) {
        log.info("Starting callOrderService for order: {} and userPid: {}", order, userPid);

        CreateOrderRequest orderRequest = createOrderRequest(order, userPid);
        log.debug("Created CreateOrderRequest: {}", orderRequest);

        try {
            orderServiceClient.createOrder(orderRequest);
            log.info("Successfully called createOrder on OrderService for order: {} and userPid: {}", order, userPid);
        } catch (StatusRuntimeException e) {
            log.error("Failed to create order on OrderService for order: {} and userPid: {}. Error: {}", order, userPid, e.getMessage());
        }
    }

    /**
     * Extracts the order ID from the pageable order response based on the given order and user PID.
     *
     * <p>This method retrieves the supplier PID from the given {@link OrderCsv} and filters the data
     * list in the {@link PageableResponse} to find the order ID that matches the given user PID.</p>
     *
     * @param order            the order data from the CSV
     * @param userPid          the user ID
     * @param pageableResponse the pageable response from which the order ID is to be extracted
     * @return an {@link ExtractedOrderIdFromPageableOrderResponse} object containing the supplier PID and order ID
     */
    private ExtractedOrderIdFromPageableOrderResponse getExtractedOrderIdFromPageableOrderResponse(OrderCsv order, String userPid, PageableResponse pageableResponse) {
        return new ExtractedOrderIdFromPageableOrderResponse(order.getSupplierPid(), pageableResponse.getDataList().stream().filter(s -> s.getUserPid().equals(userPid)).findFirst().map(OrderResponse::getPid).orElse(null));
    }

    private CreateOrderRequest createOrderRequest(OrderCsv order, String userPid) {
        Product.ProductResponse productResponse = productServiceClient.getProductByPid(of(order.getProductPid()));
        Order.Product productDetails = Order.Product.newBuilder().setPid(productResponse.getPid()).setPricePerUnit(productResponse.getPricePerUnit()).setQuantity(Integer.parseInt(order.getQuantity())).build();

        String dateCreatedStr = order.getDateCreated();
        LocalDate dateCreated = ZonedDateTime.parse(dateCreatedStr, INPUT_DATE_FORMATTER).toLocalDate();
        String formattedDateCreated = dateCreated.format(OUTPUT_DATE_FORMATTER);
        var orderStatus = order.getOrderStatus();

        return CreateOrderRequest.newBuilder().addProducts(productDetails).setUserPid(userPid).setDateCreated(of(formattedDateCreated)).setStatus(OrderStatusUtil.mapStringToOrderStatus(orderStatus)).setPricePerUnit(productResponse.getPricePerUnit()).setQuantity(Integer.parseInt(order.getQuantity())).setDateDelivered(of(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))).build();
    }

}
