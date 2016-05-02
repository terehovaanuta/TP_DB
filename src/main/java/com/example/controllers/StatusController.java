package com.example.controllers;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;

/**
 * Created by ann on 09.04.16.
 */

@RestController
public class StatusController {
    private final String COUNT_TABLE_USERS = "SELECT count(email) from users;";
    private final String COUNT_TABLE_FORUMS = "SELECT count(short_name) from forums;";
    private final String COUNT_TABLE_THREADS = "SELECT count(title) from threads;";
    private final String COUNT_TABLE_POSTS = "SELECT count(id) from posts;";
    @RequestMapping("db/api/status")
    public String statusTables() {
        Connection conn = null;
        ResultSet resultSet;
        int usersCount, forumsCount, postsCount, threadsCount;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            PreparedStatement statement = conn.prepareStatement(COUNT_TABLE_USERS);
            resultSet = statement.executeQuery();
            resultSet.first();
            usersCount = resultSet.getInt(1);
            statement = conn.prepareStatement(COUNT_TABLE_FORUMS);
            resultSet = statement.executeQuery();
            resultSet.first();
            forumsCount = resultSet.getInt(1);
            statement = conn.prepareStatement(COUNT_TABLE_THREADS);
            resultSet = statement.executeQuery();
            resultSet.first();
            threadsCount = resultSet.getInt(1);
            statement = conn.prepareStatement(COUNT_TABLE_POSTS);
            resultSet = statement.executeQuery();
            resultSet.first();
            postsCount = resultSet.getInt(1);
            JSONObject object = new JSONObject();
            JSONObject object_in = new JSONObject();
            object_in.put("user", usersCount);
            object_in.put("thread", threadsCount);
            object_in.put("forum", forumsCount);
            object_in.put("post", postsCount);
            object.put("code", 0);
            object.put("response", object_in);
            return object.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}
