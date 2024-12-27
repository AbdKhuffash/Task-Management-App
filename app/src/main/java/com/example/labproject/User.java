package com.example.labproject;

import java.io.Serializable;

public class User implements Serializable {
    private String Email;
    private String FirstName;
    private String LastName;
    private String Password;

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public User(String email, String firstName, String lastName, String password) {
        Email = email;
        FirstName = firstName;
        LastName = lastName;
        Password = password;
    }

    public User() {
    }

}
