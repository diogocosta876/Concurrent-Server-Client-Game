package com.example.assignmentdistributed;

import com.example.assignmentdistributed.user.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static List<User> users;

    public static List<User> getUsers() {
        Gson gson = new Gson();
        users = new ArrayList<>();

        try {
            String data = new String(Files.readAllBytes(Paths.get("src/users.json")));

            Type listType = new TypeToken<List<User>>() {}.getType();
            users = gson.fromJson(data, listType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public static User authenticate(String username, String password) {
        for (User user : getUsers()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                String token = TokenGenerator.generateToken(username, password);
                if(token != null && token.equals(user.getToken()))
                    return user;
            }
        }
        return null;
    }

    public static boolean register(User newUser) {
        for (User user: getUsers()) {
            if (user.getUsername().equals(newUser.getUsername())) {
                System.out.println(user);
                return false;
            }
        }

        users.add(newUser);
        writeUsersToFile();
        return true;
    }

    public static void writeUsersToFile() {
        Gson gson = new Gson();
        String json = gson.toJson(users);

        try (FileWriter writer = new FileWriter("src/users.json", false)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateGameTokens(User user1, User user2) {
        for (User user: getUsers()) {
            if(user.getUsername().equals(user1.getUsername()) || user.getUsername().equals(user2.getUsername())) {
                user.setGameToken(user1.getGameToken());
            }
        }

        writeUsersToFile();
    }

    public static void cleanGameTokens(String gameToken) {
        for (User user: getUsers()) {
            if(user.getGameToken().equals(gameToken))
                user.setGameToken("");
        }

        writeUsersToFile();
    }

    public static void updateUsersLevels(String username1, String level1, String username2, String level2) {
        for (User user: getUsers()) {
            if(user.getUsername().equals(username1))
                user.setLevel(Integer.parseInt(level1));
            if(user.getUsername().equals(username2))
                user.setLevel(Integer.parseInt(level2));
        }

        writeUsersToFile();
    }
}
