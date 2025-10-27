package com.grpc.order.server.lib.service;


import com.grpc.order.server.lib.OrderHandlerGrpc;
import com.grpc.order.server.lib.OrderInfo;
import com.grpc.order.server.lib.OrderRequest;
import com.grpc.order.server.lib.OrderStatusCode;
import com.grpc.order.server.lib.OrderStatusSubscription;
import com.grpc.order.server.lib.User;
import io.grpc.BindableService;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OrderServiceImpl extends OrderHandlerGrpc.OrderHandlerImplBase implements BindableService {

    private final static Logger log = LoggerFactory.getLogger(OrderServiceImpl.class.getName());

    AtomicInteger indexCounter = new AtomicInteger(0);

    protected Map<String, List<OrderInfo>> ordersList = new HashMap<>();
    protected List<User> userList = new ArrayList<>();

    Semaphore semaphore = new Semaphore(0);
    OrderInfo orderInfoStatus = null;
    private final OrderProcessor orderProcessor;

    //rpc OrderStatusSubscriber(OrderStatusSubscription) returns (stream OrderStatusUpdate) {};
    StreamObserver<OrderInfo> orderStatusSubscriber = null;

    // rpc SubmitOrder(OrderInfo) returns (OrderStatus) {};
    StreamObserver<OrderInfo> submitOrderObserver = null;

    boolean streamClosed = false;

    //queuing the order status for the order
    BlockingQueue<OrderInfo> orderStatusQueue = new LinkedBlockingQueue<>();

    public OrderServiceImpl(OrderProcessor orderProcessor) {
        log.info("order executor construction.. isNotNull? {}", orderProcessor!=null);
        this.orderProcessor = orderProcessor;
    }

    @PostConstruct
    private void initialize(){
       //initialize order with the current time as received time
       orderInfoStatus = OrderInfo.newBuilder()
               .setCreateTime(Instant.now().getEpochSecond())
               .setStatusCode(OrderStatusCode.RECEIVED)
               .build();

       // update the status
        orderProcessor.onOrderStatusUpdate(orderStatus -> orderStatusQueue.add(orderInfoStatus) );
    }

    @Override
    public void getOrderStatus(OrderRequest request, StreamObserver<OrderInfo> responseObserver) {
        //super.getOrderStatus(request, responseObserver);

        log.info("getOrderStatus invoked, order list {}",ordersList);
        List<OrderInfo> orders = ordersList.get(request.getUser().getUserId()).stream()
                .filter(item -> (item.getId() == request.getId()
                        || item.getUserId().equalsIgnoreCase(request.getUser().getUserId())))
                .toList();
        log.info("order list {}",orders);
        // get the last order
        OrderInfo order = null;
        if (!orders.isEmpty()){
            order = orders.getLast();
            log.info("orders found - {}",order);
        }
        responseObserver.onNext(order);
        responseObserver.onCompleted();
    }

    @Override
    public void updateOrderStatus(OrderRequest request, StreamObserver<OrderInfo> responseObserver) {

        //super.updateOrderStatus(request, responseObserver);
        if(ordersList.get(request.getUser().getUserId()).isEmpty()){
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("No orders found for "+request.getUser().getUserId())
                    .asRuntimeException());
            return;
        }else {

            //check if the order exists based on the id
            Optional<OrderInfo> order = ordersList.get(request.getUser().getUserId()).stream()
                    .filter(orderItem -> orderItem.getId() == request.getId())
                    .findFirst();
            if (order.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("No orders found for "+request.getUser().getUserId())
                        .asRuntimeException());
            }else{
                OrderInfo orderFromDB = order.get();
                OrderInfo orderInfo = OrderInfo.newBuilder()
                        .setCreateTime(Instant.now().getEpochSecond())
                        .setDescription(orderFromDB.getDescription())
                        .setId(orderFromDB.getId())
                        .setItemName(orderFromDB.getItemName())
                        .setQuantity(orderFromDB.getQuantity())
                        .setUserId(orderFromDB.getUserId())
                        .build();
            }
        }
    }

    @Override
    public void submitOrder(OrderRequest request, StreamObserver<OrderInfo> responseObserver) {
        try{

            request.getUser().getUserFirstName();
            request.getUser().getUserLastName();
            User userInfo =  User
                    .newBuilder()
                    .setUserId(request.getUser().getUserId())
                    .setUserFirstName(
                            request.getUser().getUserFirstName())
                    .setUserLastName(request.getUser().getUserLastName())
                    .build();
            if(userList.isEmpty()){
                userList.add(userInfo);
            }
            long usrCount = userList.stream().filter(userItem -> userItem.getUserId().equals(request.getUser().getUserId())).count();

            if (usrCount == 0){
                userList.add(userInfo);
            }
            ordersList.putIfAbsent(request.getUser().getUserId(), new ArrayList<>());
            OrderInfo orderInfo = OrderInfo.newBuilder()
                    .setCreateTime(Instant.now().getEpochSecond())
                    .setDescription(request.getOrderInfo().getDescription())
                    .setId(indexCounter.addAndGet(1))
                    .setItemName(request.getOrderInfo().getItemName())
                    .setQuantity(request.getOrderInfo().getQuantity())
                    .setUserId(request.getUser().getUserId())
                    .build();
                ordersList.get(request.getUser().getUserId()).add(orderInfo);

            this.streamClosed = false;
            submitOrderObserver = responseObserver;
            orderProcessor.submitOrder(request);

           }catch (Exception exception){
              responseObserver.onError(generateException(exception, Status.ABORTED));
              this.streamClosed = true;
          }
    }

    StatusException generateException(Exception exception, Status grpcStatus ){
        log.error("Exception occurred during order submission {} {}",exception.getMessage(),
                Arrays.toString(exception.getStackTrace()));
        return grpcStatus.withDescription(String.format("%s internal error: %s",
                orderProcessor.getAppServerName(),exception.getMessage()))
                .withCause(exception).asException();
    }

    @Override
    public void orderStatusSubscriber(OrderStatusSubscription request, StreamObserver<OrderInfo> responseObserverSubscriber) {
        log.info("requested status subscriber");
        this.orderStatusSubscriber = responseObserverSubscriber;

        try {
            ordersList.entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream())
                    .forEach(responseObserverSubscriber::onNext);

        } catch (Exception ex){
           responseObserverSubscriber.onError(Status.INTERNAL
                           .withDescription(String.format("Internal error %s %s"
                                   ,ex.getMessage(),Arrays.toString(ex.getStackTrace())))
                           .withCause(ex)
                   .asException());
        }
        responseObserverSubscriber.onCompleted();
    }

    /* For subscribing client
    @Scheduled(fixedDelay = 500)
    protected void statusUpdate(){
        log.debug("scheduler polling status");
        OrderInfo status;
        try{
            status = orderStatusQueue.take();
        }catch (InterruptedException ex){
            log.error("Exception occurred during status polling");
            Thread.currentThread().interrupt();
            return;
        }

        OrderInfo orderStatusUpdate = OrderInfo.newBuilder()
                .setStatusCode(status.getStatusCode())
                .build();

        if(orderStatusSubscriber != null){
            semaphore.release();
        }
        if(orderStatusSubscriber == null){
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                log.error("Interrupted Exception occurred during order subscription ");
                throw new RuntimeException(e);
            }
        }

        orderStatusSubscriber.onNext(orderStatusUpdate);
        log.debug("[subscribed]: Order Info {} ",status.getUser().getUserId());

        if(!this.streamClosed && (status.getStatusCode() == OrderStatusCode.COMPLETED
                || status.getStatusCode() == OrderStatusCode.CANCELLED)){
            this.submitOrderObserver.onNext(OrderInfo.newBuilder().getDefaultInstanceForType());
            this.submitOrderObserver.onCompleted();
            this.streamClosed = true;
        }
    }
    //*/
}
