package com.example.journalfirestore.Model;

import com.google.firebase.Timestamp;

public class Journal {

    private String userName;
    private String title;
    private String thought;
    private String imageUrl;
    private String userId;
    private Timestamp timestamp;

    public Journal(){ };

    public Journal(String userName, String title, String thought, String imageUrl, String userId, Timestamp timestamp) {
        this.userName = userName;
        this.title = title;
        this.thought = thought;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
