package com.grpc.app.data.service;

import com.grpc.app.data.dto.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusService extends JpaRepository<OrderStatus,Long> {

    List<OrderStatus> findByOrderId(long orderId);
    List<OrderStatus> findByUserName(String userName);

    List<OrderStatus> findByUserNameOrOrderId(String userName, long orderId);

}
