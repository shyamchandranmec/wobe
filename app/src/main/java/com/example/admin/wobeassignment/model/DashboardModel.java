package com.example.admin.wobeassignment.model;

import java.util.List;

/**
 * Created by Admin on 21-09-2017.
 */

public class DashboardModel {

    private String firstName;
    private String lastName;
    private String credits;
    private String emailAddress;
    private List<TransactionModel> transaction;

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

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public List<TransactionModel> getTransaction() {
        return transaction;
    }

    public void setTransaction(List<TransactionModel> transaction) {
        this.transaction = transaction;
    }
}
