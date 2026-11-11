package com.union.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    
}
