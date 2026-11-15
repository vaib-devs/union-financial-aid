package com.union.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.union.backend.dao.StudentRepository;
import com.union.backend.entity.Student;

@Service 
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public List<Student> getAllStudents() {
        List<Student> allStudents = studentRepository.findAll();
        return allStudents;
    }



    @Override
    public Student findByStudentNumber(String studentNumber) {
        Student student = studentRepository.findByStudentNumber(studentNumber).orElseThrow(() -> new IllegalArgumentException("Студент не найден"));
        return student;
    }

}
