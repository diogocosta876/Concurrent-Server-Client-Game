package com.example.assignmentdistributed.user;

import java.io.Serializable;

public class User implements Serializable {
    private final String username;
    private final String password;
    private final String token;
    private volatile String level;
    private String gameToken;

    public User(String name, String pass, String token){
        this.username = name;
        this.password = pass;
        this.token = token;
        this.level = "1";
        this.gameToken = "";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public String getLevel() {
        return level;
    }

    public String getGameToken() {
        return gameToken;
    }

    public void setLevel(Integer level) {
        this.level = level.toString();
    }

    public void setGameToken(String gameToken) {
        this.gameToken = gameToken;
    }
}
