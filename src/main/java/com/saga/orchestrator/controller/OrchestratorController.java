package com.saga.orchestrator.controller;

import com.saga.orchestrator.dto.StartSagaRequest;
import com.saga.orchestrator.service.SagaOrchestratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/saga")
public class OrchestratorController {
/*
	POST http://localhost:8880/saga/start
	Content-Type: application/json
	
	{
	  "productId": "P1001",
	  "quantity": 2
	}
* */

    private final SagaOrchestratorService orchestratorService;

    public OrchestratorController(SagaOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startSaga(@RequestBody StartSagaRequest request) {
        String sagaId = UUID.randomUUID().toString(); // Auto-generate sagaId

        orchestratorService.startOrderStep(sagaId, request); // Begin with ORDER step

        // Return structured JSON response
        Map<String, String> response = new HashMap<>();
        response.put("message", "Saga started successfully");
        response.put("sagaId", sagaId);

        return ResponseEntity.ok(response);
    }
}
