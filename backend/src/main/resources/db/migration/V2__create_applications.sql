CREATE TABLE applications(
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    period_id BIGINT NOT NULL,
    first_name VARCHAR(100) not NULL,
    last_name VARCHAR(100) not null,
    middle_name VARCHAR(100),
    birth_date DATE,
    phone VARCHAR(20),
    student_number VARCHAR(30),
    institute VARCHAR(100),
    group_name VARCHAR(10),
    registration_address VARCHAR(200),
    inn VARCHAR(12),
    bank_account VARCHAR(20),
    reason TEXT,
    status VARCHAR(30) DEFAULT 'SUBMITTED' not NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    constraint fk_applications_period FOREIGN KEY (period_id) REFERENCES application_periods(id)

);