package com.example.myapplication.Models;

public class User {
    public User(String uid, String name, String phone, String imageUrl) {
        setUserId(uid);
        setName(name);
        setPhoneNumber(phone);
        setProfileImage(imageUrl);
    }

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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    private String userId;
    private String name;
    private String phoneNumber;
    private String profileImage;

    public User() {
    }
}
