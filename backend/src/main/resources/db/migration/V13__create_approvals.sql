CREATE TABLE approvals (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id),
    approver_id BIGINT NOT NULL REFERENCES employees(id),
    action VARCHAR(20) NOT NULL,
    comment TEXT,
    decided_at TIMESTAMP NOT NULL DEFAULT NOW()
);
