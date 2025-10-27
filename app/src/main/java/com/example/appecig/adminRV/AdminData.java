package com.example.appecig.adminRV;

public class AdminData {
    String name, role, email, id;
    boolean approvement;

    public AdminData(String name, String role, String email) {
        this.name = name;
        this.role = role;
        this.email = email;
    }

    public AdminData() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getApprovement() {
        return approvement;
    }

    public void setApprovement(boolean approvement) {
        this.approvement = approvement;
    }
}