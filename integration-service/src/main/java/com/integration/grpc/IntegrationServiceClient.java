package com.integration.grpc;


import integration.IntegrationServiceGrpc;
import integration.IntegrationServiceProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static integration.IntegrationServiceProto.*;


@Service
public class IntegrationServiceClient {

    private final IntegrationServiceGrpc.IntegrationServiceBlockingStub integrationServiceBlockingStub;

    public IntegrationServiceClient(@Value("${grpc.integration-service.host}") String host,
                                    @Value("${grpc.integration-service.port}") int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        integrationServiceBlockingStub = IntegrationServiceGrpc.newBlockingStub(channel);
    }

    public String processCsvFile(String filePath) {
        ProcessCsvRequest request = ProcessCsvRequest.newBuilder()
                .setFilePath(filePath)
                .build();
        ProcessCsvResponse response = integrationServiceBlockingStub.processCsvFile(request);
        return response.getMessage();
    }
}
