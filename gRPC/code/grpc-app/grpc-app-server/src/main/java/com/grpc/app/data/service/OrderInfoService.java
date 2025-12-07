package com.grpc.app.data.service;

import com.grpc.app.data.dto.OrderInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderInfoService extends JpaRepository<OrderInfo,Long> {

    List<OrderInfo> findByOrderId(long orderId);
    List<OrderInfo> findByUserName(String userName);


}
