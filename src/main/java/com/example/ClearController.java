package com.example;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by ann on 03.04.16.
 */
@RestController
public class ClearController {

    private final String TRUNCATE_TABLE_USERS = "TRUNCATE TABLE users;";
    private final String TRUNCATE_TABLE_FORUMS = "TRUNCATE TABLE forums;";
    private final String TRUNCATE_TABLE_THREADS = "TRUNCATE TABLE threads;";
    private final String TRUNCATE_TABLE_POSTS = "TRUNCATE TABLE posts;";
    private final String TRUNCATE_SUBSCRIPTIONS = "TRUNCATE TABLE userThreadSubs;";
    private final String TRUNCATE_THREAD_VOTES = "TRUNCATE TABLE threadVotes;";
    private final String TRUNCATE_POST_VOTES = "TRUNCATE TABLE postVotes;";
    private final String TRUNCATE_USER_FOLLOW = "TRUNCATE TABLE followers;";


    @RequestMapping("db/api/clear")
    public String clearTables() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            PreparedStatement statement = conn.prepareStatement(TRUNCATE_TABLE_USERS);
            statement.execute();
            statement = conn.prepareStatement(TRUNCATE_TABLE_FORUMS);
            statement.execute();
            statement = conn.prepareStatement(TRUNCATE_TABLE_THREADS);
            statement.execute();
            statement = conn.prepareStatement(TRUNCATE_TABLE_POSTS);
            statement.execute();
            statement = conn.prepareStatement(TRUNCATE_SUBSCRIPTIONS);
            statement.execute();
            statement = conn.prepareStatement(TRUNCATE_THREAD_VOTES);
            statement.execute();
            statement = conn.prepareStatement(TRUNCATE_POST_VOTES);
            statement.execute();
            statement = conn.prepareStatement(TRUNCATE_USER_FOLLOW);
            statement.execute();

            conn.close();
            JSONObject object = new JSONObject();
            object.put("code", 0);
            object.put("response", "OK");
            return object.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}
