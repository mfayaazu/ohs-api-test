package com.integration.service;

import integration.IntegrationServiceGrpc;
import integration.IntegrationServiceProto;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static integration.IntegrationServiceProto.*;

@Service
public class IntegrationServiceImpl extends IntegrationServiceGrpc.IntegrationServiceImplBase {

    private final IntegrationService integrationService;

    public IntegrationServiceImpl(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @Override
    public void processCsvFile(ProcessCsvRequest request, StreamObserver<ProcessCsvResponse> responseObserver) {
        String filePath = request.getFilePath();
        try {
            integrationService.processCsvFile(filePath);
            ProcessCsvResponse response = ProcessCsvResponse.newBuilder()
                    .setMessage("CSV file processed successfully.")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IOException e) {
            ProcessCsvResponse response = ProcessCsvResponse.newBuilder()
                    .setMessage("Error processing CSV file: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
