package com.union.backend.dao;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.union.backend.entity.ApplicationPeriod;

public interface ApplicationPeriodRepository extends JpaRepository<ApplicationPeriod,Long> {
    Optional<ApplicationPeriod> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
        LocalDate stardDate,LocalDate endDate
    );
    



}
