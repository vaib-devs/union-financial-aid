package com.union.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.union.backend.dao.ApplicationPeriodRepository;
import com.union.backend.dao.ApplicationRepository;
import com.union.backend.entity.Application;
import com.union.backend.entity.ApplicationPeriod;
import com.union.backend.exception.ApplicationPeriodClosedException;

@Service 
public class ApplicationPeriodServiceImpl implements ApplicationPeriodService {
    private final ApplicationPeriodRepository applicationPeriodRepository;
    private final ApplicationRepository applicationRepository;
    

    public ApplicationPeriodServiceImpl(ApplicationPeriodRepository applicationPeriodRepository,ApplicationRepository applicationRepository) {
        this.applicationPeriodRepository = applicationPeriodRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public List<ApplicationPeriod> findAllApplication(){
        List<ApplicationPeriod> allApplication = applicationPeriodRepository.findAll();
        return allApplication;
    }


    @Override
    public ApplicationPeriod findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate today1,
            LocalDate today2) {
        ApplicationPeriod period = applicationPeriodRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today1, today2).orElseThrow(()-> new ApplicationPeriodClosedException("Сейчас приём заявлений закрыт"));
        return period;
    }

    

}
