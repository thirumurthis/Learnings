package com.grpc.app.server;

import com.grpc.app.data.dto.OrderInfo;
import com.grpc.app.data.dto.OrderStatus;
import com.grpc.app.exception.OrderException;
import com.proto.app.OrderDetails;
import com.proto.app.OrderKey;
import com.proto.app.OrderRequest;
import com.proto.app.OrderResponse;
import com.proto.app.OrderServiceGrpc;
import com.proto.app.OrderStatusCode;
import com.proto.app.SimRequest;
import com.proto.app.SimResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@GrpcService
public class OrderService extends OrderServiceGrpc.OrderServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class.getName());

    private final OrderHandler orderHandler;

    public OrderService(OrderHandler orderHandler) {
        this.orderHandler = orderHandler;
    }

    @Override
    public void createOrderByEmployee(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
        //super.createOrderByEmployee(request, responseObserver);

        if(validateInput(request)){
            log.info("order request received by employer ...");
            //build order info to store to db
            OrderInfo orderInfo = buildOrderInfo(request, "by_employee");
            //insert to the db
            OrderInfo savedOrderInfo = orderHandler.addOrderInfo(orderInfo);
            OrderDetails orderDetails = buildOrderDetails(savedOrderInfo);
            //construct the status to be saved to db
            OrderStatus status = buildOrderStatus(savedOrderInfo,AppConstants.RECEIVED);
            OrderStatus savedOrderStatus = orderHandler.addOrderStatus(status);
            if(savedOrderStatus != null){
                log.info("[by employee] Order status - [ orderId: {} | status: {} |" +
                                " updatedBy: {} | userName: {} | updatedAt: {} ] ",
                        savedOrderStatus.getOrderId(), savedOrderStatus.getStatus(),
                        savedOrderStatus.getUpdatedBy(), savedOrderStatus.getUserName(),
                        savedOrderStatus.getEventTime());
            } else{
                log.info("[by employee] Order Status not updated");
            }
            OrderResponse response = OrderResponse.newBuilder()
                            .addOrderResponse(orderDetails)
                                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            log.error("User Name and Item Name are mandatory");
            responseObserver.onError(new OrderException("user name and item name can't be empty!!"));
            responseObserver.onCompleted();
        }
    }

    private static OrderDetails buildOrderDetails(OrderInfo orderInfo) {
        return OrderDetails
           .newBuilder()
           .setOrderId(orderInfo.getOrderId())
           .setOrderTag(orderInfo.getOrderTag())
           .setCreatedBy(orderInfo.getCreatedBy())
           .setCreationTime(orderInfo.getCreatedAt().getTime())
           .setUpdatedBy(orderInfo.getUserName())
           .setUserName(orderInfo.getUserName())
           .setUpdateTime(orderInfo.getUpdatedAt().getTime())
           .build();
    }

    private static OrderStatus buildOrderStatus(OrderInfo orderInfoResponse, String ordStatus){

        OrderStatus status = null;
        if(ordStatus !=null && !ordStatus.isEmpty()) {
            status = new OrderStatus.OrderStatusBuilder()
               .status(getStatusCode(ordStatus).name())
               .userName(orderInfoResponse.getUserName())
               .updatedBy(orderInfoResponse.getUserName())
               .orderId(orderInfoResponse.getOrderId())
               .eventTime(Date.from(Instant.now()))
               .build();
        }
        return status;
    }

    private static OrderInfo buildOrderInfo(OrderRequest request, String userType) {
        return new OrderInfo.OrderInfoBuilder()
            .orderTag(userType)
            .itemName(request.getItemName())
            .quantity(request.getQuantity())
            .currentStatus(request.getStatus().name())
            .updatedBy(request.getUserName())
            .createdAt(new Date())
            .createdBy(request.getUserName())
            .userName(request.getUserName())
            .metadata(request.getMetadataOrDefault("order",userType))
            .updatedAt(new Date())
            .description(request.getDescription())
            .build();
    }

    private boolean validateInput(OrderRequest request) {
        // if request shouldn't be null
        // username can't be null or blank
        // item name can't be null or blank
        return (request != null
                && !request.getUserName().isEmpty()
                && !request.getItemName().isEmpty());
    }

    @Override
    public void createOrderByUser(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
        //super.createOrderByUser(request, responseObserver);
        if(validateInput(request)){
            log.info("order request received from user ...");
            //build order info to store to db
            OrderInfo orderInfo = buildOrderInfo(request, "by_user");
            //insert to the db
            OrderInfo savedOrderInfo = orderHandler.addOrderInfo(orderInfo);
            OrderDetails orderDetails = buildOrderDetails(savedOrderInfo);
            //construct the status to be saved to db
            OrderStatus status = buildOrderStatus(savedOrderInfo,AppConstants.RECEIVED);
            OrderStatus savedOrderStatus = orderHandler.addOrderStatus(status);
            if(savedOrderStatus != null){
                log.info("[by user] Order status - [ orderId: {} | status: {} |" +
                         " updatedBy: {} | userName: {} | updatedAt: {} ] ",
                        savedOrderStatus.getOrderId(), savedOrderStatus.getStatus(),
                        savedOrderStatus.getUpdatedBy(), savedOrderStatus.getUserName(),
                        savedOrderStatus.getEventTime());
            } else{
                log.info("[by user] Order Status not updated");
            }
            OrderResponse response = OrderResponse.newBuilder()
                    .addOrderResponse(orderDetails)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            log.error("[by user] User Name and Item Name are mandatory");
            responseObserver.onError(new OrderException("user name and item name can't be empty!!"));
            responseObserver.onCompleted();
        }
    }


    @Override
    public void updateOrder(OrderRequest request, StreamObserver<com.proto.app.OrderStatus> responseObserver) {
        //super.updateOrderStatus(request, responseObserver);

        try {

                if(!request.getUserName().isEmpty()) {
                    log.info("UserName : {}",request.getUserName());
                }
                if(request.getOrderId() > 0){
                    log.info("OrderId: {}",request.getOrderId());
                }
                log.info("order update request received ...");
                //build order info to store to db
                OrderInfo orderInfo = buildOrderInfo(request, "by_user");

                OrderInfo savedOrderInfo = orderHandler
                        .findOrderInfoByUserNameAndOrderId(request.getUserName(), request.getOrderId());
                //insert to the db if not present
                if (savedOrderInfo == null) {
                    log.info("order NOT found saving to database ...");
                    savedOrderInfo = orderHandler.addOrderInfo(orderInfo);
                } else {
                    log.info("order found updating in database ... username: {}, orderId: {}",
                            savedOrderInfo.getUserName(),savedOrderInfo.getOrderId());
                    orderInfo.setOrderId(savedOrderInfo.getOrderId());
                    mergeOrderInfoDetails(orderInfo, savedOrderInfo);
                    savedOrderInfo = orderHandler.updateOrderInfo(savedOrderInfo);
                }
                //OrderDetails orderDetails = buildOrderDetails(savedOrderInfo);

                //construct the status to be saved to db
                com.proto.app.OrderStatus statusCode = statusTransition(request.getStatus().name());
                //build status
                OrderStatus status = buildOrderStatus(savedOrderInfo, statusCode.getStatusCode().name());
                OrderStatus savedOrderStatus = orderHandler.addOrderStatus(status);
                if (savedOrderStatus != null) {
                    log.info("[by user] Order status - [ orderId: {} | status: {} |" +
                                    " updatedBy: {} | userName: {} | updatedAt: {} ] ",
                            savedOrderStatus.getOrderId(), savedOrderStatus.getStatus(),
                            savedOrderStatus.getUpdatedBy(), savedOrderStatus.getUserName(),
                            savedOrderStatus.getEventTime());
                } else {
                    log.info("[by user] Order Status not updated");
                }
                com.proto.app.OrderStatus statusResponse = com.proto.app.OrderStatus.newBuilder()
                        .setStatusCode(getStatusCode(savedOrderStatus.getStatus()))
                        .setUpdatedBy(savedOrderStatus.getUpdatedBy())
                        .setOrderId(savedOrderStatus.getOrderId())
                        .setEventTime(savedOrderStatus.getEventTime().getTime())
                        .build();
                responseObserver.onNext(statusResponse);
                responseObserver.onCompleted();
        }catch (Exception e){
            log.error("Error occurred",e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("[Update Order] failed with exception")
                    .withCause(e)
                    .asException());
            responseObserver.onCompleted();
        }
    }

    private void mergeOrderInfoDetails(OrderInfo orderInfo, OrderInfo savedOrderInfo) {

        if(orderInfo.getOrderTag() != null) {
            savedOrderInfo.setOrderTag(orderInfo.getOrderTag());
        }
        if(orderInfo.getDescription() != null ){
            savedOrderInfo.setDescription(orderInfo.getDescription());
        }
        if(orderInfo.getItemName() != null ){
            savedOrderInfo.setItemName(orderInfo.getItemName());
        }

        if(orderInfo.getQuantity() != savedOrderInfo.getQuantity()){
            savedOrderInfo.setQuantity(orderInfo.getQuantity());
        }
    }

    private com.proto.app.OrderStatus statusTransition(String name) {

        if (name.isEmpty()){
            return com.proto.app.OrderStatus.newBuilder()
                    .setStatusCode(getStatusCode(AppConstants.INPROGRESS))
                    .build();
        }
        if(name.equalsIgnoreCase(AppConstants.RECEIVED)){
            return com.proto.app.OrderStatus.newBuilder()
                    .setStatusCode(getStatusCode(AppConstants.INPROGRESS))
                    .build();
        }

         return com.proto.app.OrderStatus.newBuilder()
                .setStatusCode(getStatusCode(name))
                .build();
    }


    @Override
    public void getOrderStatus(OrderKey request, StreamObserver<com.proto.app.OrderStatus> responseObserver) {
        //super.getOrderStatus(request, responseObserver);

        try {
            List<OrderStatus> status = orderHandler.getOrderStatusByUserNameOrOrderId(request.getUserName(), request.getOrderId());

            if (status.isEmpty()){
                log.info("No status found for the key [ userName: {} | orderId: {} ]",
                        request.getUserName(), request.getOrderId());
               responseObserver.onCompleted();
            } else {
                status.forEach(ordStatus -> {
                    com.proto.app.OrderStatus st = com.proto.app.OrderStatus.newBuilder()
                            .setOrderId(ordStatus.getOrderId())
                            .setEventTime(ordStatus.getEventTime().getTime())
                            .setUpdatedBy(ordStatus.getUpdatedBy())
                            .setUserName(ordStatus.getUserName())
                            .setStatusCode(getStatusCode(ordStatus.getStatus()))
                            .build();
                    responseObserver.onNext(st);
                });
                responseObserver.onCompleted();
            }
        }catch (Exception e){
            log.error("Error occurred ",e);
            Status grpcErrorResponse = Status.NOT_FOUND
                    .augmentDescription("status data not found for "+request.getUserName()+"|"+request.getOrderId())
                    .withCause(e)
                    .withDescription("status data not found for "+request.getUserName()+"|"+request.getOrderId())
                                            ;
            responseObserver.onError(grpcErrorResponse.asException());
            responseObserver.onCompleted();
        }
    }

    AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void specialCaseSimulator(SimRequest request, StreamObserver<SimResponse> responseObserver) {

        Map<String,String> simReqMap = request.getSimulatorRequestMap();

        boolean simulateNetworkDelay = (simReqMap.entrySet().stream()
                .filter(entry->
                     entry.getKey().equals("simType")
                     && entry.getValue() != null
                     && entry.getValue().equalsIgnoreCase("networkDelay")
                )
                .count() >= 1.0);
        boolean simulateServerInterruption = (simReqMap.entrySet().stream()
                .filter(entry->
                        entry.getKey().equals("simType")
                                && entry.getValue() != null
                                && entry.getValue().equalsIgnoreCase("serverException")
                ).count() >= 1.0);
        if(simulateNetworkDelay){
            long delayValue;
            if(simReqMap.get("delay") == null){
                delayValue = Long.parseLong(simReqMap.get("delay"));
            } else{
                delayValue = 10000L;
            }
            try {
                delayThread(delayValue);
            }catch (Exception e){
                responseObserver
                   .onError(Status.ABORTED.withDescription("Interrupted exception occurred")
                   .asException());
            }
            SimResponse simResponse = SimResponse.newBuilder()
                    .setSimulatorResponse("Completed simulator")
                    .build();
            responseObserver.onNext(simResponse);
            responseObserver.onCompleted();
        }

        if(simulateServerInterruption){
            int num = Integer.parseInt(simReqMap.entrySet().stream()
                    .filter(entry->
                                    entry.getKey().equals("num"))
                    .map(Map.Entry::getValue)
                            .findFirst().orElse("0"));
            log.info("integer generated - {}%2 != 0? {}",counter.addAndGet(1),counter.get() % 2 != 0);
            int nwNum = ++num;
            log.info("num from request: {} ",nwNum);
            if (nwNum % 2 != 0) {
                log.info("service unavailable block");
                Status status = Status.UNAVAILABLE.withDescription("Service unavailable");
                try {
                    delayThread(3000L);
                } catch (Exception e) {
                    responseObserver.onError(Status.UNAVAILABLE
                            .withDescription("Interrupted exception occurred")
                            .asException());
                }
                log.info("Error thrown - {}", Arrays.asList(status.asRuntimeException().getStackTrace()));

                responseObserver.onError(status.asRuntimeException());
                log.info("server retry request...");
            }else{
                log.info("server exception - delayed request flow");
                try {
                    delayThread(5000L);
                } catch (Exception e) {
                    responseObserver.onError(Status.UNAVAILABLE
                            .withDescription("Interrupted exception occurred")
                            .asException());
                }
                SimResponse simResponse = SimResponse.newBuilder()
                        .setSimulatorResponse("Completed simulator")
                        .build();
                responseObserver.onNext(simResponse);
                responseObserver.onCompleted();
            }
        }
    }

    private static void delayThread(long delayInMillis){
        try {
            Thread.sleep(delayInMillis);
        } catch (InterruptedException e) {
            log.error("Interrupted exception occurred ",e);
            throw new RuntimeException("Exception in thread",e);
        }
    }

    private static com.proto.app.OrderStatusCode getStatusCode(String orderStatus ){
       return switch (orderStatus){
           case AppConstants.CREATED -> OrderStatusCode.valueOf(AppConstants.CREATED);
           case AppConstants.RECEIVED -> OrderStatusCode.valueOf(AppConstants.RECEIVED);
           case AppConstants.INPROGRESS -> OrderStatusCode.valueOf(AppConstants.INPROGRESS);
           case AppConstants.DELIVERED -> OrderStatusCode.valueOf(AppConstants.DELIVERED);
           case AppConstants.COMPLETED -> OrderStatusCode.valueOf(AppConstants.COMPLETED);
           case AppConstants.CANCELLED -> OrderStatusCode.valueOf(AppConstants.CANCELLED);
           case AppConstants.DELETED -> OrderStatusCode.valueOf(AppConstants.DELETED);
           case AppConstants.EDITED -> OrderStatusCode.valueOf(AppConstants.EDITED);
           default -> OrderStatusCode.valueOf(AppConstants.NOSTATUS);
        };
    }
}
