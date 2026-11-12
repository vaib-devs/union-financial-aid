package com.example.desktop;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

public class Application {
    private long id;
    private long periodI;
    private String firstName;
    private String lastName;
    private String middleName;
    private LocalDate birthDay;
    private String phone;
    private String group;
    private String institute;
    private String registrationAddress;
    private String inn;
    private String bankAccount;
    private Reason reason;
    private Status status;
    private OffsetDateTime createdAt;
    private String comment;

    /**
     * временный конструктор. Удалить с введеднием API
     */
    public Application(
            long id,
            long periodId,
            String firstName,
            String lastName,
            String middleName,
            LocalDate birthDay,
            String phone,
            String group,
            String institute,
            String registrationAddress,
            String inn,
            String bankAccount,
            Reason reason,
            Status status,
            OffsetDateTime createdAt,
            String comment
    ) {
        this.id = id;
        this.periodI = periodId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.birthDay = birthDay;
        this.phone = phone;
        this.group = group;
        this.institute = institute;
        this.registrationAddress = registrationAddress;
        this.inn = inn;
        this.bankAccount = bankAccount;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
        this.comment = comment;
    }

    public String applicant() {
        return lastName + " " + firstName + " " + middleName;
    }

    public String groupLine() {
        return institute + " · " + group;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPeriodI() {
        return periodI;
    }

    public void setPeriodI(long periodI) {
        this.periodI = periodI;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public LocalDate getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(LocalDate birthDay) {
        this.birthDay = birthDay;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
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

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String Id() {
        return null;
    }
}
