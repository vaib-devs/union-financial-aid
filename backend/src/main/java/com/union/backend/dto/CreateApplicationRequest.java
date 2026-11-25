package com.union.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateApplicationRequest {
    @NotBlank(message = "Номер студенческого билета не должен быть пустым")
    private String studentNumber;
    @NotBlank(message = "Номер профсоюзного билета не должен быть пустым")
    private String unionCardNumber;
    @NotBlank(message = "Не должно быть пустым")
    @Size (max = 200,message = "максимум 200 символов")
    private String registrationAddress;
    @Pattern(regexp = "[0-9]{12}")
    @NotBlank(message = "Не должно быть пустым")
    private String inn;
    @Pattern(
    regexp = "[0-9]{20}",
    message = "Банковский счёт должен содержать ровно 20 цифр"
    )
    @NotBlank(message = "Банковский счёт не должен быть пустым")
    private String bankAccount;
    
    @NotBlank(message = "Причина не должна быть пустой")
    private String reason;
    public CreateApplicationRequest() {
    }
    public String getStudentNumber() {
        return studentNumber;
    }
    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }
    public String getUnionCardNumber() {
        return unionCardNumber;
    }
    public void setUnionCardNumber(String unionCardNumber) {
        this.unionCardNumber = unionCardNumber;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public String getRegistrationAddress() {
        return registrationAddress;
    }
    public void setRegistrationAddress(String registrationAddress) {
        this.registrationAddress = registrationAddress;
    }
    public String getInn() {
        return inn;
    }
    public void setInn(String inn) {
        this.inn = inn;
    }
    public String getBankAccount() {
        return bankAccount;
    }
    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }
    

    
}
