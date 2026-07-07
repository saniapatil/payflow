-- PayFlow schema, version 1
-- This is the ONLY place the schema is defined. fintech-service runs with
-- ddl-auto=none and never alters these tables — this fixes the schema-drift
-- risk of two services independently managing the same Postgres tables.

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    phone_number  VARCHAR(20)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE accounts (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL UNIQUE REFERENCES users(id),
    -- NUMERIC instead of DOUBLE: exact decimal arithmetic, no floating-point drift
    balance     NUMERIC(19,4) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    -- optimistic-locking guard as defense in depth, on top of the Redis lock
    version     BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE transactions (
    id               BIGSERIAL PRIMARY KEY,
    -- NULL sender_id = money entering the system from outside (a deposit),
    -- not a transfer between two users. The old version used a magic
    -- sentinel (sender_id = 0) to mean the same thing, which only worked
    -- because no real user happened to have id 0 — NULL is the honest way
    -- to represent "there is no sending user" and can't collide with a
    -- real account by accident.
    sender_id        BIGINT,
    receiver_id      BIGINT NOT NULL,
    amount           NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                       CHECK (status IN ('PENDING','SUCCESS','FAILED','BLOCKED')),
    failure_reason   VARCHAR(255),
    sender_name      VARCHAR(255),
    receiver_name    VARCHAR(255),
    -- the unique constraint IS the idempotency guarantee, enforced at the DB
    -- level so a race between two retried requests can't both slip through
    idempotency_key  VARCHAR(255) NOT NULL UNIQUE,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_sender ON transactions(sender_id);
CREATE INDEX idx_transactions_receiver ON transactions(receiver_id);

CREATE TABLE ledger_entries (
    id              BIGSERIAL PRIMARY KEY,
    transaction_id  BIGINT NOT NULL REFERENCES transactions(id),
    -- NULL debit_account = the debit side is an external funding source
    -- (a deposit), not one of our own accounts.
    debit_account   BIGINT REFERENCES accounts(id),
    credit_account  BIGINT NOT NULL REFERENCES accounts(id),
    amount          NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_ledger_transaction ON ledger_entries(transaction_id);
