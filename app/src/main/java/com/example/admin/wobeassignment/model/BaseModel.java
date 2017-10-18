package com.example.admin.wobeassignment.model;

import java.math.BigInteger;

/**
 * Created by Admin on 21-09-2017.
 */

public class BaseModel {
    private String statusMessage;
    private String returnStatus;
    private BigInteger customerID;

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }

    public BigInteger getCustomerID() {
        return customerID;
    }

    public void setCustomerID(BigInteger customerID) {
        this.customerID = customerID;
    }
}
