package com.integration.grpc;

import com.google.protobuf.StringValue;
import com.integration.exception.UserAlreadyExistsException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import user.UserServiceGrpc;
import user.User;


@Service
public class UserServiceClient {

    private final UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    public UserServiceClient(@Value("${grpc.user-service.host}") String host,
                             @Value("${grpc.user-service.port}") int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        userServiceBlockingStub = UserServiceGrpc.newBlockingStub(channel);
    }

    public User.UserResponse createUser(User.CreateUserRequest request) {
        try {
            return userServiceBlockingStub.createUser(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.ALREADY_EXISTS) {
                // Handle the case where the user already exists
                throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
            }
            throw e;
        }
    }
}

