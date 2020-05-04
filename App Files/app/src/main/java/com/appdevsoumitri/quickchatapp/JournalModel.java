package com.appdevsoumitri.quickchatapp;

import com.google.firebase.Timestamp;


public class JournalModel
{
    private String title, thought, imageUrl, userID, username;
    private Timestamp timeAdded;

    public JournalModel() { } // necessity for firestore

    public JournalModel(String title, String thought, String imageUrl, String userID, String username, Timestamp timeAdded) {
        this.title = title;
        this.thought = thought;
        this.imageUrl = imageUrl;
        this.userID = userID;
        this.username = username;
        this.timeAdded = timeAdded;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getTitle() {
        return title;
    }

    public String getThought() {
        return thought;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }
}
