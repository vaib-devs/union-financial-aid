
CREATE TABLE application_periods(
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(100) not null,
    start_date DATE not null,
    end_date DATE not null,
    description TEXT,
    CONSTRAINT valid_dates check(end_date>=start_date)
);


