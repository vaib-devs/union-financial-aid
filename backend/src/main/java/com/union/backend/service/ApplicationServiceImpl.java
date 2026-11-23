package com.union.backend.service;

import java.time.LocalDate;
import java.util.Objects;


import org.springframework.stereotype.Service;


import com.union.backend.dao.ApplicationRepository;
import com.union.backend.dto.CreateApplicationRequest;
import com.union.backend.entity.Application;
import com.union.backend.entity.ApplicationPeriod;
import com.union.backend.entity.Student;
import com.union.backend.exception.InvalidUnionCardException;

@Service 
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository applicationRepository  ;
    private final StudentService studentService;
    private final ApplicationPeriodService applicationPeriodService;

    
    
    public ApplicationServiceImpl(StudentService studentService,ApplicationRepository applicationRepository,ApplicationPeriodService applicationPeriodService) {
        this.studentService = studentService;
        this.applicationRepository = applicationRepository;
        this.applicationPeriodService = applicationPeriodService;

    }

    
    @Override
    public void createApplication(CreateApplicationRequest request) {
        Student student = studentService.findByStudentNumber(request.getStudentNumber());
        if (!Boolean.TRUE.equals(student.getIsEnrolled())
    || !Boolean.TRUE.equals(student.getIsUnionMember())){
throw new IllegalArgumentException("Не прошел проверку");
        }
            String cardNumber = request.getUnionCardNumber();

            if (cardNumber == null
                    || cardNumber.isBlank()
                    || !Objects.equals(cardNumber, student.getUnionCardNumber())) {
                throw new InvalidUnionCardException("Неверный номер профсоюзного билета");
            
        }
        

        LocalDate today = LocalDate.now();
        ApplicationPeriod applicationPeriod =applicationPeriodService.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today,today);
        

        Application application = new Application();
        application.setFirstName(student.getFirstName());
        application.setLastName(student.getLastName());
        application.setMiddleName(student.getMiddleName());
        application.setBirthDate(student.getBirthDate());
        application.setPhone(student.getPhone());
        application.setStudentNumber(student.getStudentNumber());
        application.setInstitute(student.getInstitute());
        application.setGroupName(student.getGroupName());
        application.setRegistrationAddress(request.getRegistrationAddress());
        application.setInn(request.getInn());
        application.setBankAccount(request.getBankAccount());
        application.setReason(request.getReason());
        application.setApplicationPeriod(applicationPeriod);
        application.setStatus("SUBMITTED");
        applicationRepository.save(application);
        System.out.println("Добавили заявление");
        
        
    }

}
