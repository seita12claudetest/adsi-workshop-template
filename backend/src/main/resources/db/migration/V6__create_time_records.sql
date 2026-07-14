CREATE TABLE time_records (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    type VARCHAR(20) NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    office_id BIGINT REFERENCES offices(id),
    within_area BOOLEAN NOT NULL DEFAULT FALSE,
    date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_time_records_employee_date ON time_records(employee_id, date);
CREATE INDEX idx_time_records_employee_date_type ON time_records(employee_id, date, type);
