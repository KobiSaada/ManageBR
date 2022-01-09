package com.app.managebr.models;

public class MessageModel {
    private String id;
    private String message;
    private String managerId;
    private String date;

    public MessageModel() { }

    public MessageModel(String id, String message, String managerId, String date) {
        this.id = id;
        this.message = message;
        this.managerId = managerId;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getManagerId() {
        return managerId;
    }

    public String getDate() {
        return date;
    }
}
