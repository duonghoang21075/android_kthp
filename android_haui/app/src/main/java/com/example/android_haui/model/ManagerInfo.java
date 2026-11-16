package com.example.android_haui.model;

import java.io.Serializable;

/**
 * Model chứa thông tin người quản lý hệ thống phòng học từ bảng manager_info.
 */
public class ManagerInfo implements Serializable {

    private int id;
    private String staffCode;
    private String departmentName;
    private String email;
    private String role;

    public ManagerInfo() {
    }

    public ManagerInfo(int id, String staffCode, String departmentName, String email, String role) {
        this.id = id;
        this.staffCode = staffCode;
        this.departmentName = departmentName;
        this.email = email;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
