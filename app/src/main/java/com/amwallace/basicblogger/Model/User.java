package com.amwallace.basicblogger.Model;

public class User {
    private String firstname, image, lastname;

    public User() {
    }

    public User(String firstname, String image, String lastname) {
        this.firstname = firstname;
        this.image = image;
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
