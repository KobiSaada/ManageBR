package com.app.managebr.models;

import com.google.firebase.database.DatabaseError;

import java.util.Date;

public class AppliedShiftModelClass {
    private String id;
    private String empId;
    private String empToken;
    private String managerId;
    private Date date;
    private String startTime;
    private String endTime;
    private String status;
    private String checkInDate;
    private String checkOutDate;

    public AppliedShiftModelClass() { }

    public AppliedShiftModelClass(String id, String empId, String empToken, String managerId, Date date, String startTime,
                                  String endTime, String status, String checkInDate, String checkOutDate) {
        this.id = id;
        this.empId = empId;
        this.empToken = empToken;
        this.managerId = managerId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    public String getId() {
        return id;
    }

    public String getEmpId() {
        return empId;
    }

    public String getEmpToken() {
        return empToken;
    }

    public String getManagerId() {
        return managerId;
    }

    public Date getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }
}
