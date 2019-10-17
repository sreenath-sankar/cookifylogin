package com.stackroute.userloginservice.userloginservice.model;

public class MessageUser {

    private String username;
    private String name;
    private String emailId;

    public MessageUser(String username, String name, String emailId) {
        this.username = username;
        this.name = name;
        this.emailId = emailId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }
}
