package com.union.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.union.backend.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application,Long> {

    

}
