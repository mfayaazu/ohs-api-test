package com.integration.config;

import com.integration.service.IntegrationService;
import com.integration.service.IntegrationServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import user.UserServiceGrpc;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
@EnableRetry
public class GrpcServerConfig {

    private final IntegrationServiceImpl integrationServiceImpl;

    private Server server;

    public GrpcServerConfig(IntegrationServiceImpl integrationServiceImpl) {
        this.integrationServiceImpl = integrationServiceImpl;

    }

    @PostConstruct
    public void startServer() throws IOException {
        server = ServerBuilder.forPort(6565)
                .addService(integrationServiceImpl)
                .addService(ProtoReflectionService.newInstance())
                .build()
                .start();
        System.out.println("gRPC server started on port 6565");

        // Add a shutdown hook to stop the server gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (server != null) {
                System.out.println("Shutting down gRPC server...");
                server.shutdown();
                System.out.println("gRPC server shut down.");
            }
        }));
    }

    @PreDestroy
    public void stopServer() {
        if (server != null) {
            System.out.println("Shutting down gRPC server...");
            server.shutdown();
            System.out.println("gRPC server shut down.");
        }
    }
}
