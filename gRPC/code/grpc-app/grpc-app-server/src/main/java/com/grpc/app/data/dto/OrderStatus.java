package com.grpc.app.data.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name="ORDER_STATUS")
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ORDER_STATUS_SEQ")
    @SequenceGenerator(name = "ORDER_STATUS_SEQ", sequenceName = "ORDER_STATUS_SEQ", allocationSize = 1)
    @Column(name="order_status_id")
    private long orderStatusId;
    @Column(name= "user_name")
    private String userName;
    @Column(name= "order_id")
    private long orderId;
    @Column(name= "status")
    private String status;
    @Column(name= "updated_by")
    private String updatedBy;
    @Column(name= "updated_at")
    private Date eventTime;

    public OrderStatus(long orderStatusId, String userName, long orderId, String status, String updatedBy, Date eventTime) {
        this.orderStatusId = orderStatusId;
        this.userName = userName;
        this.orderId = orderId;
        this.status = status;
        this.updatedBy = updatedBy;
        this.eventTime = eventTime;
    }

    OrderStatus(OrderStatusBuilder statusBuilder){
        this.orderStatusId = statusBuilder.orderStatusId;
        this.userName = statusBuilder.userName;
        this.orderId = statusBuilder.orderId;
        this.status = statusBuilder.status;
        this.updatedBy = statusBuilder.updatedBy;
        this.eventTime = statusBuilder.eventTime;
    }

    public OrderStatus(){}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public static class OrderStatusBuilder{
        private long orderStatusId;
        private String userName;
        private long orderId;
        private String status;
        private String updatedBy;
        private Date eventTime;

        public OrderStatusBuilder setOrderStatusId(long orderStatusId) {
            this.orderStatusId = orderStatusId;
            return this;
        }

        public OrderStatusBuilder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public OrderStatusBuilder setOrderId(int orderId) {
            this.orderId = orderId;
            return this;
        }

        public OrderStatusBuilder setStatus(String status) {
            this.status = status;
            return this;
        }

        public OrderStatusBuilder setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public OrderStatusBuilder setEventTime(Date eventTime) {
            this.eventTime = eventTime;
            return this;
        }

        public OrderStatusBuilder() {}

        public OrderStatus build(){
            return new OrderStatus(this);
        }
    }
}
