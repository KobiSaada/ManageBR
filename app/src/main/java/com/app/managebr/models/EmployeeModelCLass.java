package com.app.managebr.models;

public class EmployeeModelCLass {
    private String userId;
    private String fullName;
    private String pic;
    private String email;
    private String phone;
    private String address;
    private String password;
    private String token;
    private String userType;
    private boolean emailVerified;
    private String managerId;
    private float hourSalary;

    public EmployeeModelCLass() { }

    public EmployeeModelCLass(String userId, String fullName, String pic, String email, String phone, String address,
                              String password, String token, String userType, boolean emailVerified, String managerId, float hourSalary) {
        this.userId = userId;
        this.fullName = fullName;
        this.pic = pic;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.password = password;
        this.token = token;
        this.userType = userType;
        this.emailVerified = emailVerified;
        this.managerId = managerId;
        this.hourSalary = hourSalary;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPic() {
        return pic;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public String getUserType() {
        return userType;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getManagerId() {
        return managerId;
    }

    public float getHourSalary() {
        return hourSalary;
    }
}
