CREATE TABLE saga_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    saga_id VARCHAR(255) NOT NULL,
    step VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payload TEXT,
    created_at TIMESTAMP NOT NULL
);
