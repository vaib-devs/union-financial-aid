package com.union.backend.service;

import java.time.LocalDate;

import com.union.backend.entity.ApplicationPeriod;

public interface ApplicationPeriodService {
    public ApplicationPeriod findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate today1,LocalDate today2);

}
