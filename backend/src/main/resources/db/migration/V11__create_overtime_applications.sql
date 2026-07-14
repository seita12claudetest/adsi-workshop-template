CREATE TABLE overtime_applications (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL UNIQUE REFERENCES applications(id),
    date DATE NOT NULL,
    expected_minutes INTEGER NOT NULL,
    overtime_type VARCHAR(10) NOT NULL
);
