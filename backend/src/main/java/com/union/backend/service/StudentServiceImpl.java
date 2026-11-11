package com.union.backend.service;

import java.util.List;
import java.util.Optional;

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
    public Optional<Student> findByStudentNumber(String studentNumber) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByStudentNumber'");
    }

    // @Override
    // public Optional<Student> findByStudentNumber(String studentNumber) {
    //     Optional<Student> studentOptional = studentRepository.findByStudentNumber(studentNumber);
    //     public Student student = null;
    //     if (studentOptional.isPresent()){
    //         student = studentOptional.get();
    //     }
    //     return student;
        
        
        
    // }

}
