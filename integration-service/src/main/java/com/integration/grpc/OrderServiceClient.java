package com.integration.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import order.Order;
import order.OrderServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceClient {

    private final OrderServiceGrpc.OrderServiceBlockingStub orderServiceBlockingStub;

    public OrderServiceClient(@Value("${grpc.order-service.host}") String host,
                              @Value("${grpc.order-service.port}") int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        orderServiceBlockingStub = OrderServiceGrpc.newBlockingStub(channel);
    }

    public Order.OrderResponse createOrder(Order.CreateOrderRequest request) {
        return orderServiceBlockingStub.createOrder(request);
    }

    public Order.PageableResponse getALlOrders(Order.PageableRequest request){
        return  orderServiceBlockingStub.getAllOrdersPageable(request);
    }
}