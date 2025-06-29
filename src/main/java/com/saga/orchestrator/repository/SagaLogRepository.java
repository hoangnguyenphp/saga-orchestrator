package com.saga.orchestrator.repository;

import com.saga.orchestrator.entity.SagaLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaLogRepository extends JpaRepository<SagaLog, Long> {
}
