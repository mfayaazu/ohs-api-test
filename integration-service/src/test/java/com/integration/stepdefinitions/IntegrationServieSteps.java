package com.integration.stepdefinitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.StringValue;
import com.integration.config.CucumberSpringConfiguration;
import com.integration.grpc.OrderServiceClient;
import com.integration.grpc.ProductServiceClient;
import com.integration.grpc.UserServiceClient;
import com.integration.model.OrderCsv;
import com.integration.model.ProcessedOrder;
import com.integration.service.CsvReaderService;
import com.integration.service.IntegrationService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import order.Order;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import product.Product;
import user.User;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = CucumberSpringConfiguration.class)
public class IntegrationServieSteps {

    @Mock
    private CsvReaderService csvReaderService;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private OrderServiceClient orderServiceClient;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private IntegrationService integrationService;

    private String csvFilePath;

    @Given("the CSV file {string} is loaded")
    public void theCSVFileIsLoaded(String inputFilePath) {
        csvFilePath = inputFilePath;
        MockitoAnnotations.openMocks(this);
    }

    @When("the IntegrationService processes the CSV file")
    public void theIntegrationServiceProcessesTheCSVFile() throws IOException {
        // Mocking the necessary services and methods
        List<OrderCsv> orders = List.of(
                new OrderCsv("1", "John", "Doe", "john.doe@example.com", "supplier1", "4111111111111111", "VISA", "order1", "product1", "123 Main St", "USA", "2022-01-01T00:00:00Z", "10", "John Doe", "0"),
                new OrderCsv("2", "Jane", "Smith", "jane.smith@example.com", "supplier2", "4111111111111111", "MASTERCARD", "order2", "product2", "456 Elm St", "USA", "2022-01-02T00:00:00Z", "5", "Jane Smith", "1")
        );


        when(csvReaderService.readOrders(csvFilePath)).thenReturn(orders);

        for(OrderCsv order : orders) {
            User.UserResponse userResponse = User.UserResponse.newBuilder().setPid("user-id").build();
            when(userServiceClient.createUser(any())).thenReturn(userResponse);

            Product.ProductResponse productResponse = Product.ProductResponse.newBuilder().setPid("product-id").build();
            when(productServiceClient.getProductByPid(StringValue.of(order.getProductPid()))).thenReturn(productResponse);

            Order.PageableResponse pageableResponse = Order.PageableResponse.newBuilder()
                    .addData(Order.OrderResponse.newBuilder().setUserPid("user-id").setPid("order-id").build())
                    .build();
            when(orderServiceClient.getALlOrders(any())).thenReturn(pageableResponse);

            integrationService.processCsvFile(csvFilePath);
        }
    }

    @Then("the orders should be created successfully")
    public void theOrdersShouldBeCreatedSuccessfully() {
        // Verify that the createUser and createOrder methods were called
        verify(userServiceClient, times(4)).createUser(any());
        verify(orderServiceClient, times(4)).getALlOrders(any());
    }

    @And("the json with userId orderId and supplierId should be created in path {string}")
    public void theJsonWithUserIdOrderIdAndSupplierIdShouldBeCreated(String outputFilePath) throws IOException {
        File file = new File(outputFilePath);
        Assertions.assertTrue(file.exists(), "JSON file does not exist");

        // Read and parse the JSON file
        ObjectMapper mapper = new ObjectMapper();
        ProcessedOrder[] processedOrders = mapper.readValue(file, ProcessedOrder[].class);

        // Verify the content of the JSON file
        Assertions.assertNotNull(processedOrders, "Processed orders should not be null");
        Assertions.assertTrue(processedOrders.length > 0, "Processed orders should not be empty");
    }
}
