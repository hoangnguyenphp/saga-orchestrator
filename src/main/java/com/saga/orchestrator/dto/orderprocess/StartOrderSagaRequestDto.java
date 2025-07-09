package com.saga.orchestrator.dto.orderprocess;

import com.saga.orchestrator.dto.SagaRequestDto;

public class StartOrderSagaRequestDto extends SagaRequestDto {
    private String productId;
    private Integer quantity;
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
}
