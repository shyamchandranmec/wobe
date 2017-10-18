package com.example.admin.wobeassignment.model;

import java.math.BigInteger;

/**
 * Created by Admin on 21-09-2017.
 */

public class TransactionModel {
    private String transactionId;
    private String fromCustomerID;
    private String fromFirstName;
    private String fromLastName;
    private String toCustomerID;
    private String toFirstName;
    private String toLastName;
    private float credits;
    private String transactionDate;
    private String description;
    private String noteToSelf;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public String getFromCustomerID() {
        return fromCustomerID;
    }

    public void setFromCustomerID(String fromCustomerID) {
        this.fromCustomerID = fromCustomerID;
    }

    public String getToCustomerID() {
        return toCustomerID;
    }

    public void setToCustomerID(String toCustomerID) {
        this.toCustomerID = toCustomerID;
    }
    public String getFromFirstName() {
        return fromFirstName;
    }

    public void setFromFirstName(String fromFirstName) {
        this.fromFirstName = fromFirstName;
    }

    public String getFromLastName() {
        return fromLastName;
    }

    public void setFromLastName(String fromLastName) {
        this.fromLastName = fromLastName;
    }


    public String getToFirstName() {
        return toFirstName;
    }

    public void setToFirstName(String toFirstName) {
        this.toFirstName = toFirstName;
    }

    public String getToLastName() {
        return toLastName;
    }

    public void setToLastName(String toLastName) {
        this.toLastName = toLastName;
    }

    public float getCredits() {
        return credits;
    }

    public void setCredits(float credits) {
        this.credits = credits;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNoteToSelf() {
        return noteToSelf;
    }

    public void setNoteToSelf(String noteToSelf) {
        this.noteToSelf = noteToSelf;
    }
}
