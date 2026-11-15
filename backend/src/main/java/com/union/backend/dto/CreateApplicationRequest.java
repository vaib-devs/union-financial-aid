package com.union.backend.dto;

public class CreateApplicationRequest {
    private String studentNumber;
    private String unionCardNumber;
    private String registrationAddress;
    private String inn;
    private String bankAccount;
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
