package com.integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.integration.config.IntegrationProperties;
import com.integration.handler.OrderServiceHandler;
import com.integration.handler.UserServiceHandler;
import com.integration.model.OrderCsv;
import com.integration.model.ProcessedOrder;
import com.integration.model.Result;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class IntegrationService {

    private final CsvReaderService csvReaderService;

    private final UserServiceHandler userServiceHandler;
    private final OrderServiceHandler orderServiceHandler;
    private final IntegrationProperties integrationProperties;


    public IntegrationService(CsvReaderService csvReaderService,
                              UserServiceHandler userServiceHandler,
                              OrderServiceHandler orderServiceHandler, IntegrationProperties integrationProperties) {
        this.csvReaderService = csvReaderService;
        this.userServiceHandler = userServiceHandler;
        this.orderServiceHandler = orderServiceHandler;
        this.integrationProperties = integrationProperties;

    }

    /**
     * Processes the CSV file to create users and orders.
     *
     * @param filePath the path to the CSV file
     * @throws IOException if an I/O error occurs
     */
    @Retryable(retryFor = {IOException.class, StatusRuntimeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void processCsvFile(String filePath) throws IOException {
        List<OrderCsv> orders = csvReaderService.readOrders(filePath);
        List<ProcessedOrder> processedOrders = new ArrayList<>();

        createUsersAndOrders(orders, processedOrders);
        WriteProcessedOrdersToOutputFile(processedOrders);
    }

    /**
     * Creates users and orders based on the provided list of orders.
     *
     * <p>This method processes each order in the given list, attempting to create a user and an order
     * for each one. If user creation or order retrieval fails, the order is skipped. Successfully
     * processed orders are added to the list of processed orders.</p>
     *
     * @param orders          the list of orders to process
     * @param processedOrders the list to which successfully processed orders are added
     */
    private void createUsersAndOrders(List<OrderCsv> orders, List<ProcessedOrder> processedOrders) {
        for (OrderCsv order : orders) {
            var fetchUserPid = getUserIdFromUserService(order);
            if (fetchUserPid.isEmpty()) {
                log.warn("Skipping order due to user creation failure: {} ", order);
                continue;
            }
            String userPid = fetchUserPid.get();

            var fetchOrderAndSupplierId = getOrderAndSupplierId(order, fetchUserPid.get());
            if (fetchOrderAndSupplierId.isEmpty()) {
                log.warn("Skipping order due to order/supplier retrieval failure: {}", order);
                continue;
            }
            var orderServiceResult = fetchOrderAndSupplierId.get();
            processedOrders.add(new ProcessedOrder(userPid, orderServiceResult.orderId(), orderServiceResult.supplierPid()));
        }
    }

    /**
     * Retrieves the user ID by creating a new user via the UserService.
     *
     * @param order the order data from the CSV
     * @return the user ID or null if creation failed
     */
    @Retryable(retryFor = {StatusRuntimeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    private Optional<String> getUserIdFromUserService(OrderCsv order) {
        try {
            return userServiceHandler.callUserService(order);
        } catch (StatusRuntimeException e) {
            log.error("UserId Already Exists. User-Service returned 409 Conflict ");
        }
        return Optional.empty();
    }


    /**
     * Retrieves the order and supplier ID by querying the OrderService.
     *
     * @param order   the order data from the CSV
     * @param userPid the user ID
     * @return a Result object containing the supplier ID and order ID
     */
    @Retryable(retryFor = {StatusRuntimeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    private Optional<Result> getOrderAndSupplierId(OrderCsv order, String userPid) {
        try {
            log.info("Calling Order-Service");
            return orderServiceHandler.callOrderService(order, userPid);
        } catch (StatusRuntimeException e) {
            log.error("Error occurred when calling OrderService");
        }
        return Optional.empty();
    }

    private void WriteProcessedOrdersToOutputFile(List<ProcessedOrder> processedOrders) {
        try {
            writeProcessedOrdersToJsonFile(processedOrders);
            log.info("ProcessedOrders jSON File Created Successfully");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Writes the list of processed orders to a JSON file.
     *
     * @param processedOrders the list of processed orders
     */
    private void writeProcessedOrdersToJsonFile(List<ProcessedOrder> processedOrders) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

        String outputFilePath = integrationProperties.getOutputFilePath();
        Path outputPath = Paths.get(outputFilePath).toAbsolutePath();
        Path parentDir = outputPath.getParent();

        try {
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                log.info("Created directories for path: {}", parentDir.toAbsolutePath());
            }

            writer.writeValue(outputPath.toFile(), processedOrders);
            log.info("Processed orders written to {}", outputFilePath);
        } catch (IOException e) {
            log.error("Error writing processed orders to JSON: {}", e.getMessage());
        }
    }


}
