package com.union.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.union.backend.entity.Student;
import com.union.backend.service.StudentService;

@RestController 
public class StudentController {

    private final StudentService studentService;
    

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }


    @GetMapping("/api/students")
    public List<Student> getAllStudents(){
        List<Student> allStudents = studentService.getAllStudents();
        return allStudents;
    }

    @GetMapping("/api/students/{studentNumber}")
    public Student findByStudentNumber(@PathVariable String studentNumber){
        return studentService.findByStudentNumber(studentNumber);
    }

    


    
}
