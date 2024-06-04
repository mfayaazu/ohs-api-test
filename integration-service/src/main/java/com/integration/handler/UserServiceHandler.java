package com.integration.handler;

import com.integration.grpc.UserServiceClient;
import com.integration.model.OrderCsv;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import user.User;

import java.util.Optional;

import static com.google.protobuf.StringValue.of;

@Service
@Slf4j
public class UserServiceHandler {

    private final UserServiceClient userServiceClient;

    public UserServiceHandler(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }


    /**
     * Calls the UserService to create a user and retrieve the user PID.
     *
     * <p>This method creates a user request from the given {@link OrderCsv} and sends it to the
     * UserService. If the user is created successfully, the user PID is returned. In case of an error,
     * it logs the error message and returns null.</p>
     *
     * @param order the order data from the CSV
     * @return the user PID or null if creation failed
     */
    public Optional<String> callUserService(OrderCsv order) {
        User.CreateUserRequest userRequest = createUserRequest(order);
        try {
            log.info("Sending CreateUserRequest to UserService: {}", userRequest);
            User.UserResponse userResponse = userServiceClient.createUser(userRequest);
            log.info("Received UserResponse from UserService: {}", userResponse);
            return Optional.of(userResponse.getPid());

        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.ALREADY_EXISTS) {
                log.warn("User already exists: {}", e.getMessage());
            } else {
                log.error("Error creating user: {}", e.getMessage());
            }
        }
        return Optional.empty();
    }

    private static User.CreateUserRequest createUserRequest(OrderCsv order) {
        return User.CreateUserRequest.newBuilder()
                .setFullName(of(order.getFirstName() + " " + order.getLastName()))
                .setEmail(order.getEmail())
                .setAddress(User.ShippingAddress.newBuilder()
                        .setAddress(of(order.getShippingAddress()))
                        .setCountry(of(order.getCountry()))
                        .build())
                .addPaymentMethods(User.PaymentMethod.newBuilder()
                        .setCreditCardNumber(of(order.getCreditCardNumber()))
                        .setCreditCardType(of(order.getCreditCardType()))
                        .build())
                .build();
    }

}
