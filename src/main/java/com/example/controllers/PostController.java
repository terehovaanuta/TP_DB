package com.example.controllers;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;

/**
 * Created by ann on 07.04.16.
 */

@RestController
public class PostController {
    private final String CREATE_QUERY = "INSERT INTO posts (parent, isApproved, isHighlighted, isEdited, isSpam, isDeleted, date, thread, message, user, forum) VALUES (?, ?, ?, ?, ?, ?, DATE(?), ?, ?, ?, ?);";
    private final String GET_USER_QUERY = "SELECT * FROM users WHERE email = ?;";
    private final String GET_FORUM_QUERY = "SELECT * FROM forums WHERE short_name = ?;";

    // According to documentation, we take thread by id
    private final String GET_THREAD_QUERY = "SELECT * FROM threads WHERE id = ?;";
    private final String GET_POST_QUERY = "SELECT * FROM posts WHERE message = ?;";
    @RequestMapping("db/api/post/create")
    public String createPost(@RequestBody String payload) {
        JSONObject object = new JSONObject(payload);
        System.out.println(payload);
        String date, message, user, forum;
        Integer thread;
        Integer parent = null;
        Boolean isApproved = null;
        Boolean isHighlighted = null;
        Boolean isEdited = null;
        Boolean isSpam = null;
        Boolean isDeleted = null;

        try {
            date = object.getString("date");
            message = object.getString("message");
            user = object.getString("user");
            forum = object.getString("forum");
            thread = object.getInt("thread");
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        }

        try {
            parent = object.getInt("parent");
            isApproved = object.getBoolean("isApproved");
            isHighlighted = object.getBoolean("isHighlighted");
            isEdited = object.getBoolean("isEdited");
            isSpam = object.getBoolean("isSpam");
            isDeleted = object.getBoolean("isDeleted");
        } catch (Exception e) {
            parent = null;
            isApproved = null;
            isHighlighted = null;
            isEdited = null;
            isSpam = null;
            isDeleted = null;
        }

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            PreparedStatement statement = conn.prepareStatement(CREATE_QUERY);

            // We need to set values separately, if they are null
//            if (isDeleted != null || parent != null || isApproved != null || isHighlighted != null || isEdited != null || isSpam != null) {
//                statement.setInt(1, parent);
//                statement.setBoolean(2, isApproved);
//                statement.setBoolean(3, isHighlighted);
//                statement.setBoolean(4, isEdited);
//                statement.setBoolean(5, isSpam);
//                statement.setBoolean(6, isDeleted);
//            } else {
//                // TODO: think about making it null
//                // statement.setInt(1, false);
//                // I make it null, because default value is null
//                statement.setNull(1, Types.INTEGER);
//                statement.setBoolean(2, false);
//                statement.setBoolean(3, false);
//                statement.setBoolean(4, false);
//                statement.setBoolean(5, false);
//                statement.setBoolean(6, false);
//            }
            if (isDeleted != null) {
                statement.setBoolean(6, isDeleted);
            } else {
                statement.setBoolean(6, false);
            }
            if (parent != null) {
                statement.setInt(1, parent);
            } else {
                statement.setNull(1, Types.INTEGER);
            }
            if (isApproved != null) {
                statement.setBoolean(2, isApproved);
            } else {
                statement.setBoolean(2, false);
            }
            if (isHighlighted != null) {
                statement.setBoolean(3, isHighlighted);
            } else {
                statement.setBoolean(3, false);
            }
            if (isEdited != null) {
                statement.setBoolean(4, isEdited);
            } else {
                statement.setBoolean(4, false);
            }
            if (isSpam != null) {
                statement.setBoolean(5, isSpam);
            } else {
                statement.setBoolean(5, false);
            }

            statement.setString(7, date);
            statement.setInt(8, thread);
            statement.setString(9, message);
            statement.setString(10, user);
            statement.setString(11, forum);


            PreparedStatement getStatementUser = conn.prepareStatement(GET_USER_QUERY);
            getStatementUser.setString(1, user);
            ResultSet isUserCreated = getStatementUser.executeQuery();
            PreparedStatement getStatementForum = conn.prepareStatement(GET_FORUM_QUERY);
            getStatementForum.setString(1, forum);
            ResultSet isForumCreated = getStatementForum.executeQuery();
            PreparedStatement getStatementThread = conn.prepareStatement(GET_THREAD_QUERY);
//            getStatementThread.setString(1, thread);
            getStatementThread.setInt(1, thread);
            ResultSet isThreadCreated = getStatementThread.executeQuery();
            if (!isUserCreated.next() || !isForumCreated.next() || !isThreadCreated.next()) {
                JSONObject error = new JSONObject();
                error.put("code", 5);
                error.put("response", "such user or forum not exists");
                return error.toString();
            }

            int numberOfRowsAffected = statement.executeUpdate();
            PreparedStatement getStatement = conn.prepareStatement(GET_POST_QUERY);
            getStatement.setString(1, message);
            ResultSet newPost = getStatement.executeQuery();
            if (newPost.next()) {
                JSONObject wrappedObject = new JSONObject();
                object = new JSONObject();
                object.put("id", newPost.getInt("id"));
//                object.put("date", newPost.getString("date"));
                object.put("date", newPost.getDate("date"));
                object.put("message", newPost.getString("message"));
                object.put("user", newPost.getString("user"));
                object.put("forum", newPost.getString("forum"));
                object.put("thread", newPost.getInt("thread"));
                object.put("parent", newPost.getInt("parent"));
                object.put("isApproved", newPost.getBoolean("isApproved"));
                object.put("isHighlighted", newPost.getBoolean("isHighlighted"));
                object.put("isEdited", newPost.getBoolean("isEdited"));
                object.put("isSpam", newPost.getBoolean("isSpam"));
                object.put("isDeleted", newPost.getBoolean("isDeleted"));

                wrappedObject.put("code", 0);
                wrappedObject.put("response", object);
                System.out.printf(wrappedObject.toString());
                return wrappedObject.toString();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Duplicate");
            return error.toString();
        }

        return "";
    }

}
