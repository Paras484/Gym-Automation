package com.paw.gymautomation;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class User implements Serializable {
    private String userId; // New field to store Firebase key
    private String name;
    private String phoneNumber;
    private String startDate;
    private String expiryDate;

    public User() {
        // Required default constructor for Firebase
    }

    public User(String userId, String name, String phoneNumber, String startDate, String expiryDate) {
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
    }

    // Getters and setters for all fields

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
