package com.union.backend.service;

import java.util.List;
import java.util.Optional;

import com.union.backend.entity.Student;

public interface StudentService {
    public List<Student> getAllStudents();
    public Optional<Student> findByStudentNumber(String studentNumber);
    
}
