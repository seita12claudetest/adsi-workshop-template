CREATE TABLE time_correction_applications (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL UNIQUE REFERENCES applications(id),
    date DATE NOT NULL,
    original_clock_in TIME,
    original_clock_out TIME,
    corrected_clock_in TIME,
    corrected_clock_out TIME
);
