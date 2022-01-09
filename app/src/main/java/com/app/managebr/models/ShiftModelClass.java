package com.app.managebr.models;

import java.util.Date;

public class ShiftModelClass {
    private String id;
    private Date date;
    private String startTime;
    private String endTime;
    private int numberOfEmp;
    private String status;
    private String managerId;
    private String managerToken;

    public ShiftModelClass() { }

    public ShiftModelClass(String id, Date date, String startTime, String endTime, int numberOfEmp, String status, String managerId, String managerToken) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numberOfEmp = numberOfEmp;
        this.status = status;
        this.managerId = managerId;
        this.managerToken = managerToken;
    }

    public String getId() {
        return id;
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

    public int getNumberOfEmp() {
        return numberOfEmp;
    }

    public String getStatus() {
        return status;
    }

    public String getManagerId() {
        return managerId;
    }

    public String getManagerToken() {
        return managerToken;
    }
}
