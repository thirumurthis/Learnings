package com.spring.grpc.client;

import com.grpc.order.server.lib.OrderHandlerGrpc;
import com.grpc.order.server.lib.OrderInfo;
import com.grpc.order.server.lib.OrderRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class OrderGrpcClientImpl implements OrderGrpcClient{

    private static Logger log = LoggerFactory.getLogger(OrderGrpcClientImpl.class.getName());

    private StreamObserver<OrderInfo> orderInfoResponseObserver;

    private OrderHandlerGrpc.OrderHandlerStub clientStub;
    private OrderHandlerGrpc.OrderHandlerBlockingStub blockClientStub;

    private ManagedChannel managedChannel = null;

    private ScheduledFuture<?> future = null;

    boolean subscriptionStatusFailed;


    public OrderGrpcClientImpl(OrderHandlerGrpc.OrderHandlerStub clientStub, OrderHandlerGrpc.OrderHandlerBlockingStub blockingStub){
        this.blockClientStub = blockingStub;
        this.clientStub = clientStub;
    }

    void subscribtionPolling(){
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        future = executorService.scheduleWithFixedDelay(
                this::subscriptionRetry,1000,1000, TimeUnit.SECONDS
        );

    }

    private void subscriptionRetry(){
     if(subscriptionStatusFailed){
         subscriptionStatusFailed = false;
         log.info("subscribe failed server might not be ready retry attempt");
         subscribe();
     }
    }

    private OrderHandlerGrpc.OrderHandlerStub stub(ManagedChannelBuilder<?> builder){
        managedChannel = builder.defaultServiceConfig(clientGrpcProperties.get)
    }

    @Override
    public void submitOrder(OrderRequest orderRequest) {

    }

    @Override
    public void getOrderStatus(OrderRequest orderRequest) {

    }

    @Override
    public void subscribe() {

    }
}
