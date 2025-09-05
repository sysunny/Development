-- Flyway migration: create transactions table if it does not exist and ensure idempotency key column + unique index

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender VARCHAR(255),
    receiver VARCHAR(255),
    amount DECIMAL(19,2),
    currency VARCHAR(10),
    gateway_type VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP,
    idempotency_key VARCHAR(255)
);

-- Ensure unique index on the idempotency key to enforce deduplication
CREATE UNIQUE INDEX IF NOT EXISTS idx_transactions_idempotency_key ON transactions(idempotency_key);
