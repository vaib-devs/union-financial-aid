package com.union.backend.service;

import java.time.LocalDate;
import java.util.List;

import com.union.backend.dao.ApplicationPeriodRepository;
import com.union.backend.dao.ApplicationRepository;
import com.union.backend.entity.ApplicationPeriod;

public interface ApplicationPeriodService {
    public ApplicationPeriod findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate today1,LocalDate today2);
    public List<ApplicationPeriod> findAllApplication();

}
