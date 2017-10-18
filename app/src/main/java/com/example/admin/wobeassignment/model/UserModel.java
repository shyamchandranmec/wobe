package com.example.admin.wobeassignment.model;

import com.example.admin.wobeassignment.activities.RegisterActivity;
import com.example.admin.wobeassignment.utilities.Constants;
import com.example.admin.wobeassignment.utilities.SharedPreferenceManager;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shyam on 10/10/17.
 */

public class UserModel {
    private String firstName;
    private String lastName;
    private String email;
    private String userId;
    private float credits;
    private float sent;
    private float received;
    private float added;
    private List<TransactionModel> transactions = new ArrayList<>();
   // private Map<String, TransactionModel> transactionMap;
    public UserModel() {

    }
    public UserModel(String firstName, String lastName, String email, String userId, float credits, float sent, float received, float added, List transactions) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userId = userId;
        this.credits = credits;
        this.sent = sent;
        this.received = received;
        this.added = added;
        if(transactions != null) {
            this.transactions = transactions;
        }

        /*if(transactionMap.size() == 0) {
            this.transactions = new ArrayList<>();
        } else {
            this.transactions = new ArrayList<TransactionModel>(transactionMap.values());
        }*/

    }


    public float getReceived() {
        return received;
    }

    public void setReceived(float received) {
        this.received = received;
    }
    public float getSent() {
        return sent;
    }

    public void setSent(float sent) {
        this.sent = sent;
    }

    public float getAdded() {
        return added;
    }

    public void setAdded(float added) {
        this.added = added;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public float getCredits() {
        return credits;
    }

    public void setCredits(float credits) {
        this.credits = credits;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public List<TransactionModel> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionModel> transactions) {
        this.transactions = transactions;
    }
    public void syncTransactionMap() {
        List<TransactionModel> list;
        Map<String,TransactionModel> map = new HashMap<String,TransactionModel>();
        for (TransactionModel i : this.transactions) {
            map.put(i.getTransactionId(),i);
        }

    }

}
