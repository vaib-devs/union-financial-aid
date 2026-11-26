package com.union.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.union.backend.dto.CreateApplicationRequest;
import com.union.backend.entity.Application;
import com.union.backend.entity.ApplicationPeriod;
import com.union.backend.service.ApplicationPeriodService;
import com.union.backend.service.ApplicationService;

import jakarta.validation.Valid;

@RestController 
public class ApplicationController {
    

    private final ApplicationService applicationService;
    private final ApplicationPeriodService applicationPeriodService;
    public ApplicationController(ApplicationService applicationService,ApplicationPeriodService applicationPeriodService) {
        this.applicationService = applicationService;
        this.applicationPeriodService = applicationPeriodService;
        
    }


    @PostMapping("/api/applications") 
    public void createApplication(@Valid @RequestBody CreateApplicationRequest request){
        applicationService.createApplication(request);
    }

    @GetMapping("/api/application")
    public List<Application> findAllApplication(){
        List<Application> allApplication = applicationService.findAllApplication();
        return allApplication; 
    }

    @GetMapping("api/applicationperiod")
    public List<ApplicationPeriod> findAllApplicationPeriods(){
        List<ApplicationPeriod> allApplication = applicationPeriodService.findAllApplication();
        return allApplication; 
    }


}
