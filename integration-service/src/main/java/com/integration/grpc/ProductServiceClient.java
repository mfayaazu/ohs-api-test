package com.integration.grpc;

import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import order.Order;
import order.OrderServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import product.Product;
import product.ProductServiceGrpc;

@Service
public class ProductServiceClient {

    private final ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub;

    public ProductServiceClient(@Value("${grpc.product-service.host}") String host,
                                @Value("${grpc.product-service.port}") int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        productServiceBlockingStub = ProductServiceGrpc.newBlockingStub(channel);
    }

    public Product.ProductResponse getProductByPid(StringValue request) {
        return productServiceBlockingStub.getProductByPid(request);
    }
}