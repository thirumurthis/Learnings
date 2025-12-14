package com.grpc.app.data.service;

import com.grpc.app.data.dto.OrderInfo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderInfoService extends JpaRepository<OrderInfo,Long> {

    List<OrderInfo> findByOrderId(long orderId);
    List<OrderInfo> findByUserName(String userName);
    OrderInfo findByUserNameAndOrderId(String userName, long orderId);

    @Transactional
    default OrderInfo updateOrInsert(OrderInfo info){
        return save(info);
    }

}
