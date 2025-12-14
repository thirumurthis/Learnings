/*
package com.spring.grpc.client;

import com.grpc.order.lib.OrderHandlerGrpc;
import com.grpc.order.lib.OrderInfo;
import com.grpc.order.lib.OrderRequest;
import com.grpc.order.lib.OrderStatusSubscription;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class OrderGrpcClientImpl implements OrderGrpcClient{

    private static Logger log = LoggerFactory.getLogger(OrderGrpcClientImpl.class.getName());

    private StreamObserver<OrderInfo> orderInfoResponseObserver;

    private String address;

    private OrderHandlerGrpc.OrderHandlerStub clientStub;
    private OrderHandlerGrpc.OrderHandlerBlockingStub blockClientStub;
    private GrpcProperties properties;

    private ManagedChannel managedChannel = null;

    private ScheduledFuture<?> future = null;

    private  volatile boolean subscriptionStatusFailed;
    private volatile boolean initialized;

    public OrderGrpcClientImpl(String address, GrpcProperties properties){
        this.address = address;
        this.properties = properties;
        log.info("invoking the initializeStubs...");
        initializeStubs(address);
        pollableReSubscription();
    }

    public OrderGrpcClientImpl(OrderHandlerGrpc.OrderHandlerStub clientStub,
                               OrderHandlerGrpc.OrderHandlerBlockingStub blockingStub,
                               GrpcProperties properties){
        this.blockClientStub = blockingStub;
        this.clientStub = clientStub;
        this.properties = properties;
        pollableReSubscription();
    }

    private void initializeStubs(String address){

        log.info("initializeStubs method invoked.. ");
        clientStub = stub(address,properties);
        blockClientStub = blockingStub(address,properties);
    }

    void pollableReSubscription(){
        log.info("invoke pollableReSubscription ");
        try (ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor()) {

            future = executorService.scheduleWithFixedDelay(
                    this::subscriptionRetry, 1000, 1000, TimeUnit.MILLISECONDS
            );
        }

    }

    private void subscriptionRetry(){
        log.info("subscriptionRetry method invoked... ");
     if(subscriptionStatusFailed){
         subscriptionStatusFailed = false;
         log.info("subscribe failed server might not be ready retry attempt");
         subscribe();
     }

    }
    private void reSubscribe(){
        if(subscriptionStatusFailed){
            subscriptionStatusFailed = false;
            subscribe();
        }
    }

    private OrderHandlerGrpc.OrderHandlerStub stub(String address, GrpcProperties properties){

        managedChannel = ClientManagedChannelBuilder.target(address,properties)
                .build();
        return OrderHandlerGrpc.newStub(managedChannel);

    }

    private OrderHandlerGrpc.OrderHandlerBlockingStub blockingStub(String address,
                                                                   GrpcProperties properties){
        managedChannel = ClientManagedChannelBuilder.target(address,properties).build();
        return OrderHandlerGrpc.newBlockingStub(managedChannel);
    }

    public void submitOrder(OrderRequest orderRequest) {

        this.clientStub.submitOrder(orderRequest,this.orderInfoResponseObserver);

    }

    public void getOrderStatus(OrderRequest orderRequest) {

    }

    public void subscribe() {

        try {
            subscribeForStatus();
        }catch (Exception e){
            log.error("subscription status failed");
            subscriptionStatusFailed = true;
        }
        this.initialized = true;
    }

    public void updateOrderStatus(OrderRequest orderRequest) {


    }


    void shutdown(){
        if(managedChannel != null){
            try{
                managedChannel.shutdown().awaitTermination(10,TimeUnit.SECONDS);

            }catch (InterruptedException e){
                log.warn("Interrupted exception ",e);
                Thread.currentThread().interrupt();
            }
        }
    }
    @Override
    public void subscribeForStatus() {
        log.info("invoke subscribeForStatus orderInfoResponseObserver.isNull? {}",orderInfoResponseObserver==null);

        if(orderInfoResponseObserver == null){

            orderInfoResponseObserver = new StreamObserver<>() {
                @Override
                public void onNext(OrderInfo orderInfo) {
                    var status = orderInfo.getStatusCode().getDescriptorForType().getName();
                    log.info("onNext status of order: {}",status);
                }

                @Override
                public void onError(Throwable throwable) {
                    log.warn("Error occurred {}", Arrays.toString(throwable.getStackTrace()));
                    orderInfoResponseObserver = null;
                    subscriptionStatusFailed = true;
                }

                @Override
                public void onCompleted() {
                    orderInfoResponseObserver = null;

                }
            };
        }
        try{
            clientStub.orderStatusSubscriber(OrderStatusSubscription.newBuilder().build(), orderInfoResponseObserver);
        }catch (StatusRuntimeException e){
            log.error("error occurred {}",Arrays.toString(e.getStackTrace()));
            throw new RuntimeException("error occurred "+Arrays.toString(e.getStackTrace()));
        }
    }
}
*/
