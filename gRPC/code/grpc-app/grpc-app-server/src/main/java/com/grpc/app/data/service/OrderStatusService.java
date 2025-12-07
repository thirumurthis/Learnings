package com.grpc.app.data.service;

import com.grpc.app.data.dto.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusService extends JpaRepository<OrderStatus,Long> {

    List<OrderStatus> findByOrderId(long orderId);
    List<OrderStatus> findByUserName(String userName);

    List<OrderStatus> findByUserNameOrOrderId(String userName, long orderId);
}
