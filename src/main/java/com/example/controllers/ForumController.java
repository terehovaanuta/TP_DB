package com.example.controllers;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;

/**
 * Created by ann on 03.04.16.
 */
@RestController
public class ForumController {
    private final String CREATE_QUERY = "INSERT INTO forums (user, name, short_name) VALUES (?, ?, ?);";
    private final String GET_USER_QUERY = "SELECT * FROM users WHERE email = ?;";
    private final String GET_FORUM_QUERY = "SELECT * FROM forums WHERE short_name = ?;";
    @RequestMapping("db/api/forum/create")
    public String createForum(@RequestBody String payload) {
        JSONObject object = new JSONObject(payload);
        String name, short_name, user;
        try {
            name = object.getString("name");
            short_name = object.getString("short_name");
            user = object.getString("user");
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        }

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            PreparedStatement statement = conn.prepareStatement(CREATE_QUERY);
            statement.setString(1, user);
            statement.setString(2, name);
            statement.setString(3, short_name);
            PreparedStatement getStatement = conn.prepareStatement(GET_USER_QUERY);
            getStatement.setString(1, user);
            ResultSet isUserCreated = getStatement.executeQuery();
            if (!isUserCreated.next()) {
                JSONObject error = new JSONObject();
                error.put("code", 5);
                error.put("response", "such user not exists");
                return error.toString();
            }
            int numberOfRowsAffected = statement.executeUpdate();
            getStatement = conn.prepareStatement(GET_FORUM_QUERY);
            getStatement.setString(1, short_name);
            ResultSet newForum = getStatement.executeQuery();
            if (newForum.next()) {
                JSONObject wrappedObject = new JSONObject();
                object = new JSONObject();
                object.put("id", newForum.getInt("id"));
                object.put("name", newForum.getString("name"));
                object.put("short_name", newForum.getString("short_name"));
                object.put("user", newForum.getString("user"));
                wrappedObject.put("code", 0);
                wrappedObject.put("response", object);
                System.out.println("RETURN REQUEST");
                return wrappedObject.toString();
            }
        } catch (SQLException e) {
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Duplicate");
            return error.toString();
        }
        return "";
    }
}
