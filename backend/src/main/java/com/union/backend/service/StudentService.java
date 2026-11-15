package com.union.backend.service;

import java.util.List;


import com.union.backend.entity.Student;

public interface StudentService {
    public List<Student> getAllStudents();
    public Student findByStudentNumber(String studentNumber);
    
}
