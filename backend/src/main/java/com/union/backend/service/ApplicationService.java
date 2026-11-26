package com.union.backend.service;



import java.util.List;

import com.union.backend.dto.CreateApplicationRequest;
import com.union.backend.entity.Application;


public interface ApplicationService {
    public void createApplication(CreateApplicationRequest request);
    public List<Application> findAllApplication();

}
