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
@Table(name="ORDER_INFO")
public class OrderInfo {

    public OrderInfo(OrderInfoBuilder builder){
        this.orderId = builder.orderId;
        this.userName = builder.userName;
        this.orderTag = builder.orderTag;
        this.itemName = builder.itemName;
        this.quantity = builder.quantity;
        this.description = builder.description;
        this.metadata = builder.metadata;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.createdBy = builder.createdBy;
    }

    public OrderInfo(long orderId,String userName, String orderTag, String itemName,
                     long quantity, String description, String metadata,
                     Date createdAt, Date updatedAt, String createdBy) {
        this.orderId = orderId;
        this.userName = userName;
        this.orderTag = orderTag;
        this.itemName = itemName;
        this.quantity = quantity;
        this.description = description;
        this.metadata = metadata;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
    }

    public OrderInfo(){}

    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ORDER_INFO_SEQ")
    @SequenceGenerator(name = "ORDER_INFO_SEQ", sequenceName = "ORDER_SEQ", allocationSize = 1)
    @Column(name= "order_id")
    private long orderId;
    @Column(name="user_name")
    private String userName;
    @Column(name= "order_tag")
    private String orderTag;
    @Column(name= "item_name")
    private String itemName;
    @Column(name= "item_quantity")
    private long quantity;
    @Column(name= "description")
    private String description;
    @Column(name= "metadata")
    private String metadata;
    @Column(name= "created_at")
    private Date createdAt;
    @Column(name= "updated_at")
    private Date updatedAt;
    @Column(name= "created_by")
    private String createdBy;


    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOrderTag() {
        return orderTag;
    }

    public void setOrderTag(String orderTag) {
        this.orderTag = orderTag;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public static class OrderInfoBuilder{

        private long orderId;
        private String userName;
        private String orderTag;
        private String itemName;
        private long quantity;
        private String description;
        private String metadata;
        private Date createdAt;
        private Date updatedAt;
        private String createdBy;

        public OrderInfoBuilder orderId(long orderId) {
            this.orderId = orderId;
            return this;
        }

        public OrderInfoBuilder orderTag(String orderTag) {
            this.orderTag = orderTag;
            return this;
        }

        public OrderInfoBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public OrderInfoBuilder itemName(String itemName) {
            this.itemName = itemName;
            return this;
        }

        public OrderInfoBuilder quantity(long quantity) {
            this.quantity = quantity;
            return this;
        }

        public OrderInfoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public OrderInfoBuilder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }

        public OrderInfoBuilder createdAt(Date createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public OrderInfoBuilder updatedAt(Date updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public OrderInfoBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        //public OrderInfoBuilder(){}
        public OrderInfo build(){
            return new OrderInfo(this);
           // return new OrderInfo(orderId, orderTag, itemName,
           //  quantity, description, metadata,
           //  createdAt, updatedAt, createdBy);
        }

    }
}
