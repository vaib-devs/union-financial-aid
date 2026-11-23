package com.union.backend.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.union.backend.dao.ApplicationPeriodRepository;
import com.union.backend.entity.ApplicationPeriod;

@Service 
public class ApplicationPeriodServiceImpl implements ApplicationPeriodService {
    private final ApplicationPeriodRepository applicationPeriodRepository;
    

    public ApplicationPeriodServiceImpl(ApplicationPeriodRepository applicationPeriodRepository) {
        this.applicationPeriodRepository = applicationPeriodRepository;
    }


    @Override
    public ApplicationPeriod findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate today1,
            LocalDate today2) {
        ApplicationPeriod period = applicationPeriodRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today1, today2).orElseThrow(()-> new IllegalArgumentException("Сейчас приём заявлений закрыт"));
        return period;
    }

    

}
