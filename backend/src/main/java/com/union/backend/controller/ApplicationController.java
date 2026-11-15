package com.union.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.union.backend.dto.CreateApplicationRequest;
import com.union.backend.service.ApplicationService;

@RestController 
public class ApplicationController {
    

    private final ApplicationService applicationService;
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }


    @PostMapping("/api/applications") 
    public void createApplication(@RequestBody CreateApplicationRequest request){
        applicationService.createApplication(request);
    }

}
