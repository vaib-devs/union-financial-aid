CREATE table students(
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    first_name VARCHAR(100) not NULL,
    last_name VARCHAR(100) not null,
    middle_name VARCHAR(100),
    birth_date DATE,
    phone VARCHAR(20),
    student_number VARCHAR(30) UNIQUE NOT NULL,
    institute VARCHAR(100),
    group_name VARCHAR(10),
    is_enrolled BOOLEAN Not NULL,
    is_union_member BOOLEAN NOT NULL DEFAULT FALSE

);