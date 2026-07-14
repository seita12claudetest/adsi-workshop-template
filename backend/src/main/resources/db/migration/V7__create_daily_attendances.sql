CREATE TABLE daily_attendances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    date DATE NOT NULL,
    clock_in TIME,
    clock_out TIME,
    break_start TIME,
    break_end TIME,
    working_minutes INTEGER,
    overtime_minutes INTEGER,
    break_minutes INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (employee_id, date)
);
