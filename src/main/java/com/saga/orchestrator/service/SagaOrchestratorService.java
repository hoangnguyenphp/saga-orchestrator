package com.saga.orchestrator.service;

import com.saga.orchestrator.dto.StartSagaRequest;
import com.saga.orchestrator.entity.SagaLog;
import com.saga.orchestrator.repository.SagaLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class SagaOrchestratorService {

    private final RestTemplate restTemplate;
    private final SagaLogRepository sagaLogRepository;

    @Autowired
    public SagaOrchestratorService(RestTemplate restTemplate, SagaLogRepository sagaLogRepository) {
        this.restTemplate = restTemplate;
        this.sagaLogRepository = sagaLogRepository;
    }

    public void startOrderStep(String sagaId, StartSagaRequest request) {
        // STEP 1: ORDER
        logStep(sagaId, "ORDER", "STARTED", "Creating order");
        String orderServiceUrl = "http://localhost:8881/orders";
        Map<String, Object> orderPayload = Map.of(
                "productId", request.getProductId(),
                "quantity", request.getQuantity(),
                "sagaId", sagaId
        );

        try {
            ResponseEntity<String> orderResponse = restTemplate.postForEntity(
                    orderServiceUrl,
                    new HttpEntity<>(orderPayload),
                    String.class
            );
            logStep(sagaId, "ORDER", "COMPLETED", orderResponse.getBody());
        } catch (Exception ex) {
            logStep(sagaId, "ORDER", "FAILED", ex.getMessage());
            return;
        }

        // STEP 2: INVENTORY
        logStep(sagaId, "INVENTORY", "STARTED", "Reserving inventory");
        String inventoryUrl = String.format(
                "http://localhost:8882/inventory/reserve?productId=%s&quantity=%d",
                request.getProductId(), request.getQuantity()
        );

        try {
            ResponseEntity<String> inventoryResponse = restTemplate.postForEntity(
                    inventoryUrl, null, String.class
            );
            if (inventoryResponse.getStatusCode().is2xxSuccessful()) {
                logStep(sagaId, "INVENTORY", "COMPLETED", inventoryResponse.getBody());
            } else {
                logStep(sagaId, "INVENTORY", "FAILED", inventoryResponse.getBody());
                compensateOrder(sagaId, request);
                return;
            }
        } catch (Exception ex) {
            logStep(sagaId, "INVENTORY", "FAILED", ex.getMessage());
            compensateOrder(sagaId, request);
            return;
        }

        // STEP 3: PAYMENT
        logStep(sagaId, "PAYMENT", "STARTED", "Processing payment");
        String paymentUrl = String.format(
                "http://localhost:8883/payments/process?sagaId=%s&productId=%s&amount=%d",
                sagaId, request.getProductId(), request.getQuantity() * 100 // assume fixed price
        );

        try {
            ResponseEntity<String> paymentResponse = restTemplate.postForEntity(
                    paymentUrl, null, String.class
            );
            if (paymentResponse.getStatusCode().is2xxSuccessful()) {
                logStep(sagaId, "PAYMENT", "COMPLETED", paymentResponse.getBody());
            } else {
                logStep(sagaId, "PAYMENT", "FAILED", paymentResponse.getBody());
                compensatePayment(sagaId, request);
                compensateInventory(sagaId, request);
                compensateOrder(sagaId, request);
            }
        } catch (Exception ex) {
            logStep(sagaId, "PAYMENT", "FAILED", ex.getMessage());
            compensatePayment(sagaId, request);
            compensateInventory(sagaId, request);
            compensateOrder(sagaId, request);
        }
    }

    
    private void compensateOrder(String sagaId, StartSagaRequest request) {
        logStep(sagaId, "ORDER_COMPENSATION", "STARTED", "Cancelling order");

        try {
            String cancelOrderUrl = String.format(
                    "http://localhost:8881/orders/cancel?sagaId=%s&productId=%s",
                    sagaId, request.getProductId()
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    cancelOrderUrl, HttpMethod.PUT, null, String.class
            );

            logStep(sagaId, "ORDER_COMPENSATION", "COMPLETED", response.getBody());
        } catch (Exception ex) {
            logStep(sagaId, "ORDER_COMPENSATION", "FAILED", ex.getMessage());
        }
    }

    private void compensateInventory(String sagaId, StartSagaRequest request) {
        logStep(sagaId, "INVENTORY_COMPENSATION", "STARTED", "Releasing inventory reservation");

        try {
            String cancelInventoryUrl = String.format(
                    "http://localhost:8882/inventory/cancel?productId=%s&quantity=%d",
                    request.getProductId(), request.getQuantity()
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    cancelInventoryUrl, HttpMethod.PUT, null, String.class
            );

            logStep(sagaId, "INVENTORY_COMPENSATION", "COMPLETED", response.getBody());
        } catch (Exception ex) {
            logStep(sagaId, "INVENTORY_COMPENSATION", "FAILED", ex.getMessage());
        }
    }

    private void compensatePayment(String sagaId, StartSagaRequest request) {
        logStep(sagaId, "PAYMENT_COMPENSATION", "STARTED", "Refunding payment");

        try {
            String refundUrl = String.format(
                    "http://localhost:8883/payments/refund?sagaId=%s&productId=%s",
                    sagaId, request.getProductId()
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    refundUrl, HttpMethod.PUT, null, String.class
            );

            logStep(sagaId, "PAYMENT_COMPENSATION", "COMPLETED", response.getBody());
        } catch (Exception ex) {
            logStep(sagaId, "PAYMENT_COMPENSATION", "FAILED", ex.getMessage());
        }
    }


    //Add retry mechanism for failed compensation
    private void logStep(String sagaId, String step, String status, String payload) {
        SagaLog log = new SagaLog();
        log.setSagaId(sagaId);
        log.setStep(step);
        log.setStatus(status);
        log.setPayload(payload);
        sagaLogRepository.save(log);
    }
}
