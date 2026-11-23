package com.union.backend.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.union.backend.entity.Student;


public interface StudentRepository extends JpaRepository<Student,Long>{
    public Optional<Student> findByStudentNumber(String studentNumber);
}


