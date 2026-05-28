package com.secureshare.model;

public class User {
    private String username;
    private String fullName;
    private String password;
    private String publicKeyPath;

    public User(String username, String fullName, String password, String publicKeyPath) {
        this.username = username;
        this.fullName = fullName;
        this.password = password;
        this.publicKeyPath = publicKeyPath;
    }

    // Getters
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getPassword() { return password; }
    public String getPublicKeyPath() { return publicKeyPath; }
}