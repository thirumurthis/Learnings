package com.grpc.app.server;

import com.grpc.app.data.dto.OrderInfo;
import com.grpc.app.data.dto.OrderStatus;
import com.proto.app.OrderResponse;

import java.util.List;

public interface OrderHandler {

    List<OrderInfo> findOrderInfoForUser(String userName);
    OrderInfo addOrderInfo(OrderInfo orderInfo);
    List<OrderStatus> getOrderStatusByUserNameOrOrderId(String userName, long orderId);
}
