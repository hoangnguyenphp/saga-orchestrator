package com.saga.orchestrator.dto.orderprocess;

import com.saga.orchestrator.dto.SagaRequestDto;

public class OrderRequestDto extends SagaRequestDto {
    private String sagaId;
    private String productId;
    private int quantity;
    private String type; // e.g. ORDER_CREATE, ORDER_CANCEL

    public OrderRequestDto() {
    }

    public OrderRequestDto(String sagaId, String productId, int quantity, String type) {
        this.sagaId = sagaId;
        this.productId = productId;
        this.quantity = quantity;
        this.type = type;
    }

    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "OrderEvent{" +
                "sagaId='" + sagaId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", type='" + type + '\'' +
                '}';
    }
}
