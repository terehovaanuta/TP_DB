package com.example.models;

/**
 * Created by ann on 03.04.16.
 */
public class User {

    private final Integer id;
    private final String about;
    private final String email;
    private final String username;
    private final String name;

    public User(Integer id, String email, String username, String name, String about) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.name = name;
        this.about  = about;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getAbout() {
        return about;
    }

}
