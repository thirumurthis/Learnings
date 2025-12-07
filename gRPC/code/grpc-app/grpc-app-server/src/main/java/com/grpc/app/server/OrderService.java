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
import io.grpc.stub.StreamObserver;
import org.apache.juli.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

        if(request.getUserName() != null &&  request.getItemName() != null ){
            log.info("order request received..");
            OrderInfo orderInfo = new OrderInfo.OrderInfoBuilder()
                    .orderTag("by_employee")
                    .itemName(request.getItemName())
                    .quantity(request.getQuantity())
                    .createdAt(new Date())
                    .createdBy(request.getUserName())
                    .userName(request.getUserName())
                    .updatedAt(new Date())
                    .description(request.getDescription())
                    .build();
            orderHandler.addOrderInfo(orderInfo);
            OrderDetails orderDetails = OrderDetails
                    .newBuilder()
                    .setOrderId(orderInfo.getOrderId())
                    .setOrderTag(orderInfo.getOrderTag())
                    .setCreatedBy(orderInfo.getCreatedBy())
                    .setCreationTime(orderInfo.getCreatedAt().getTime())
                    .setUpdatedBy(orderInfo.getUserName())
                    .setUserName(orderInfo.getUserName())
                    .setUpdateTime(orderInfo.getUpdatedAt().getTime())
                    .build();
            OrderResponse response = OrderResponse.newBuilder()
                            .addOrderResponse(orderDetails)
                                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        }else {
            log.error("User Name and Item Name are mandatory");
            responseObserver.onError(new OrderException("user name and item name can't be empty!!"));
        }

    }

    @Override
    public void createOrderByUser(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
        super.createOrderByUser(request, responseObserver);
    }

    @Override
    public void getOrderStatus(OrderKey request, StreamObserver<com.proto.app.OrderStatus> responseObserver) {
        //super.getOrderStatus(request, responseObserver);

        try {
            List<OrderStatus> status = orderHandler.getOrderStatusByUserNameOrOrderId(request.getUserName(), request.getOrderId());

            status.forEach(ordStatus -> {
                com.proto.app.OrderStatus st = com.proto.app.OrderStatus.newBuilder()
                        .setOrderId(ordStatus.getOrderId())
                        .setEventTime(ordStatus.getEventTime().getTime())
                        .setUpdatedBy(ordStatus.getUpdatedBy())
                        .setStatusCode(getStatusCode(ordStatus.getStatus()))
                        .build();
                responseObserver.onNext(st);
            });

            responseObserver.onCompleted();
        }catch (Exception e){
            responseObserver.onError(e);
        }
    }

    private static com.proto.app.OrderStatusCode getStatusCode(String orderStatus ){
       return switch (orderStatus){
            case "CREATED" -> OrderStatusCode.valueOf("CREATED");
            case "RECEIVED" -> OrderStatusCode.valueOf("RECEIVED");
            case "IN_PROGRESS" -> OrderStatusCode.valueOf("IN_PROGRESS");
            case "DELIVERED" -> OrderStatusCode.valueOf("DELIVERED");
            case "COMPLETED" -> OrderStatusCode.valueOf("COMPLETED");
            case "CANCELLED" -> OrderStatusCode.valueOf("CANCELLED");
            case "DELETED" -> OrderStatusCode.valueOf("DELETED");
            default -> OrderStatusCode.valueOf("IN_PROGRESS");
        };
    }

    @Override
    public void updateOrderStatus(OrderRequest request, StreamObserver<com.proto.app.OrderStatus> responseObserver) {
        super.updateOrderStatus(request, responseObserver);
    }
}
