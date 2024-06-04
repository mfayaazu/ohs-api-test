# Order Integration Service

This document provides a runbook for setting up and running the Order Integration Service. Follow the steps below to ensure the service is up and running correctly.

## Prerequisites

- Docker
- Docker Compose
- Java 17
- Maven
- grpcurl

## Steps to Run the Order Integration Service

### Step 1: Run Docker Compose

First, ensure that your Docker services are up and running. Navigate to the root directory of the project where the `docker-compose.yml` file is located and run:

```bash
docker-compose up
```
This command will start all the required services, including the User Service, Order Service, Product Service, and any other dependencies.

### Step 2: Start the Spring Boot Application
Next, start the Spring Boot application OrderIntegrationApp. You can run the application using Maven:

```bash
mvn spring-boot:run
```
Alternatively, you can run the compiled JAR file:

```bash
java -jar target/integration-service-0.0.1-SNAPSHOT.jar
```
Ensure that the application starts without any errors and is listening on the configured port (default is 1234).

### Step 3: List the gRPC Services
To verify that the gRPC service is running, use grpcurl to list the services available on localhost:6565:

```bash
grpcurl -plaintext localhost:6565 list
```
You should see integrationService listed as one of the services.

### Step 4: Make a gRPC Request with CSV File as Input
To process the orders from the CSV file located at src/main/resources/input/order-integration.csv, use grpcurl to make a gRPC request to the integrationService:

```bash
 grpcurl -plaintext -d '{                         
  "filePath": "src/main/resources/input/order-integration.csv"
}' localhost:6565 integration.IntegrationService/ProcessCsvFile
```
This command sends the contents of the CSV file to the ProcessCsvFile method of the integrationService.

## Troubleshooting
If you encounter any issues, check the following:

* Ensure all Docker services are running correctly.
* Verify that the Spring Boot application started without errors.
* Confirm that the CSV file is located at src/main/resources/input/order-integration.csv and is formatted correctly.
* For further assistance, consult the README.pdf or reach out to the developer.

Logging
The application uses SLF4J for logging. Ensure that the appropriate log level is set in the application.yml or the environment-specific configuration files (application-local.yml, application-dev.yml, application-prod.yml).

Profiles
The application supports different profiles for various environments. Set the spring.profiles.active property to switch between profiles:

```bash
-Dspring.profiles.active=local
-Dspring.profiles.active=dev
-Dspring.profiles.active=prod
```
This allows the application to use different configurations for different environments.

Follow these steps to ensure the Order Integration Service is set up and running correctly. If you have any questions or issues, please refer to the project documentation or contact the support team.

### Additional Notes:
* Ensure the file `order-integration.csv` is in the specified directory (`src/main/resources/input/`).
* If you are running the commands from a different directory, consider using absolute paths to avoid issues related to relative paths.
* The command `ls -l` is used to check if the file exists and has the correct path. Adjust the path if necessary based on your project structure.

If the file is still not found, ensure that the directory and file names are correctly spelled and accessible from your terminal.


