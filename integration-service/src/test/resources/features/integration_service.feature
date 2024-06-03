Feature: Integration Service

  Scenario: Process CSV file and create orders
    Given the CSV file "src/test/resources/input/order-integration-test.csv" is loaded
    When the IntegrationService processes the CSV file
    Then the orders should be created successfully
    And the json with userId orderId and supplierId should be created in path "src/test/resources/output/processed-order-test.json"
