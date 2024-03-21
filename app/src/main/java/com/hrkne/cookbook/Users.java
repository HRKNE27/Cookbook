package com.hrkne.cookbook;

public class Users {
    String ProfileImage, Status, FullName;

    public Users(){

    }

    public Users(String profileImage, String status, String fullName) {
        ProfileImage = profileImage;
        Status = status;
        FullName = fullName;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String profileImage) {
        ProfileImage = profileImage;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }
}
