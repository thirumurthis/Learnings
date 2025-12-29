package com.grpc.app.server;

import com.grpc.app.data.dto.OrderInfo;
import com.grpc.app.data.dto.OrderStatus;
import com.grpc.app.data.service.OrderInfoService;
import com.grpc.app.data.service.OrderStatusService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderHandlerImpl implements OrderHandler{

    private final OrderInfoService orderInfoService;
    private final OrderStatusService orderStatusService;
    public OrderHandlerImpl(OrderInfoService orderInfoService, OrderStatusService orderStatusService) {
        this.orderInfoService = orderInfoService;
        this.orderStatusService = orderStatusService;
    }

    @Override
    public List<OrderInfo> findOrderInfoForUser(String userName) {
        return orderInfoService.findByUserName(userName);
    }

    @Override
    public OrderInfo addOrderInfo(OrderInfo orderInfo) {
        return orderInfoService.save(orderInfo);
    }

    @Override
    public List<OrderStatus> getOrderStatusByUserNameOrOrderId(String userName, long orderId) {
       return orderStatusService.findByUserNameOrOrderId(userName,orderId);
    }

    @Override
    public OrderInfo findOrderInfoByUserNameAndOrderId(String userName, long orderId) {
        return orderInfoService.findByUserNameAndOrderId(userName,orderId);
    }

    @Override
    public OrderStatus addOrderStatus(OrderStatus status) {
        return orderStatusService.save(status);
    }

    @Override
    public OrderInfo updateOrderInfo(OrderInfo info){
        return orderInfoService.updateOrInsert(info);
    }

/*
    @Override
    public List<OrderInfo> getOrderInfoByUserName(String userName) {
        return List.of();
    }

    @Override
    public List<OrderInfo> getOrderInfoByOrderId(long orderId) {
        return List.of();
    }

    @Override
    public OrderInfo saveOrder(OrderInfo orderInfo) {
        return null;
    }*/

    /*
    @Override
    public OrderStore getOrderDetailsByUserId(String userId) {
        orderInfoService.findByUserName(userId);
        return null;
    }

    @Override
    public OrderStore getOrderDetailsByOrderId(String orderId) {
        return null;
    }

    @Override
    public OrderStore getOrderDetailsByUserIdOrOrderId(String userId, String orderId) {
        return null;
    }

    @Override
    public OrderStore getOrderDetailsByUserIdAndOrderId(String userId, String orderId) {
        return null;
    }

    @Override
    public OrderResponse saveOrder(OrderRequest orderRequest) throws OrderException {
        return null;
    }

    @Override
    public boolean updateStatus(String userId, String orderId, OrderStatusCode statusCode) {
        return false;
    }

    @Override
    public List<OrderStatus> getOrderStatusInfo(String userId, String orderId) {
        return List.of();
    }

     */
/*
    @Override
    public OrderResponse saveOrder(OrderRequest orderRequest) throws OrderException{

        //check if the user exists
        if(Objects.isNull(userInfoList.get(orderRequest.getUserName()))){
            throw new OrderException("User NOT found, create new user first.");
        }

        User userInfo = userInfoList.get(orderRequest.getUserName());

        log.info("User: {} - isAdmin user? {}",userInfo.getUserName(),userInfo.getUserType());
        if(orderList.isEmpty()){
            OrderStore order = new OrderStore();

            long orderIdIndex = getIndexLongValue();
            String orderTag = String.valueOf(orderIdIndex);
            OrderInfo info = OrderInfo
                    .newBuilder()
                    .setOrderId(orderIdIndex)
                    .setItemName(orderRequest.getItemName())
                    .setDescription(orderRequest.getDescription())
                    .setQuantity(orderRequest.getQuantity())
                    .build();
            OrderStore details = new OrderStore();
            details.getOrders().put(String.valueOf(orderIdIndex),info);
            if(details.getStatuses().isEmpty()){
                OrderStatus status = OrderStatus.newBuilder()
                        .setUserName(orderRequest.getUserName())
                        .setStatusCode(OrderStatusCode.RECEIVED)
                        .build();
                List<OrderStatus> ordStatus = new ArrayList<>();
                ordStatus.add(status);
               // details.getStatuses().put(orderIdIndex,ordStatus);

            }
        }else{
            OrderStore details = orderList.get(orderRequest.getUserName());

            if(details == null){
                long orderIdIndex = getIndexLongValue();
                OrderInfo orderInfo = OrderInfo
                        .newBuilder()
                        .setOrderId(orderIdIndex)
                        .setItemName(orderRequest.getItemName())
                        .setDescription(orderRequest.getDescription())
                        .setQuantity(orderRequest.getQuantity())
                        .build();
                OrderStore orderStore = new OrderStore();
                //details.getOrders().put(orderIdIndex,orderInfo);
                orderList.put(orderRequest.getUserName(), orderStore);
            }

        }

        //orderList.putIfAbsent(orderRequest.getUserId(),orderRequest);
        return null;
    }

    @Override
    public boolean addUser(User user) {
        if(userInfoList.isEmpty()){
            userInfoList.put(user.getUserName(),user);
        }else{
            //find the user exists or not then add
            userInfoList.putIfAbsent(user.getUserName(), user);
        }
        //if user exists return tru or false
        return userInfoList.get(user.getUserName()) != null;
    }

    @Override
    public boolean updateStatus(String userId, String orderId, OrderStatusCode statusCode) {
        return false;
    }

    @Override
    public List<OrderStatus> getOrderStatusInfo(String userId, String orderId) {
        return List.of();
    }


    String getIndexValue(){
      return String.valueOf(indexCounter.addAndGet(1));
    }

    long getIndexLongValue(){
        return indexCounter.addAndGet(1);
    }
     */
}
