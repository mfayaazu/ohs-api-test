# Command to access integration service

 ```
 grpcurl -plaintext -d '{"filePath": "/Users/fayaazuddinalimohammad/Downloads/ohs-api-test/integration-service/src/main/resources/order-integration.csv"
}' localhost:6565 integration.IntegrationService/ProcessCsvFile ```
