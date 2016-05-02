package com.example.controllers;

import com.example.DbUtils;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by ann on 03.04.16.
 */

@RestController
public class ThreadController {
    private final String CREATE_QUERY = "INSERT INTO threads (isDeleted, forum, title, isClosed, user, date, message, slug) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
    private final String GET_USER_QUERY = "SELECT * FROM users WHERE email = ?;";
    private final String GET_FORUM_QUERY = "SELECT * FROM forums WHERE short_name = ?;";
    private final String GET_THREAD_QUERY = "SELECT * FROM threads WHERE title = ?;";

    @RequestMapping("db/api/thread/create")
    public String createThread(@RequestBody String payload) {
        JSONObject object = new JSONObject(payload);
        System.out.println(payload);
        String forum, title, user, date, message, slug;
        boolean isClosed;
        Boolean isDeleted = null;

        try {
            forum = object.getString("forum");
            title = StringEscapeUtils.escapeJava(object.getString("title"));
            user = object.getString("user");
            date = object.getString("date");
            message = object.getString("message");
            slug = object.getString("slug");
            isClosed = object.getBoolean("isClosed");
//            isDeleted = object.getBoolean("isDeleted");
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        }
        System.out.println("Good string");

        try {
            isDeleted = object.getBoolean("isDeleted");
        } catch (Exception e) {
            isDeleted = null;
        }
        System.out.println("Very good string");

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            PreparedStatement statement = conn.prepareStatement(CREATE_QUERY);
            if (isDeleted != null) {
                statement.setBoolean(1, isDeleted);
            } else {
                // TODO: think about making it null
                statement.setBoolean(1, false);
            }
            statement.setString(2, forum);
            statement.setString(3, title);
            statement.setBoolean(4, isClosed);
            statement.setString(5, user);
            System.out.println(date);
            statement.setTimestamp(6, Timestamp.valueOf(date));
//            statement.setString(6, date);
            statement.setString(7, message);
            statement.setString(8, slug);
            PreparedStatement getStatementUser = conn.prepareStatement(GET_USER_QUERY);
            getStatementUser.setString(1, user);
            ResultSet isUserCreated = getStatementUser.executeQuery();
            PreparedStatement getStatementForum = conn.prepareStatement(GET_FORUM_QUERY);
            getStatementForum.setString(1, forum);
            ResultSet isForumCreated = getStatementForum.executeQuery();
            if (!isUserCreated.next() || !isForumCreated.next()) {
                JSONObject error = new JSONObject();
                error.put("code", 5);
                error.put("response", "such user or forum not exists");
                return error.toString();
            }

            int numberOfRowsAffected = statement.executeUpdate();
            PreparedStatement getStatement = conn.prepareStatement(GET_THREAD_QUERY);
            getStatement.setString(1, title);
            ResultSet newThread = getStatement.executeQuery();
            System.out.println("Excellent string");
            if (newThread.next()) {
                System.out.println("Beautiful string");
                JSONObject wrappedObject = new JSONObject();
                object = new JSONObject();
                object.put("id", newThread.getInt("id"));
                object.put("isDeleted", newThread.getBoolean("isDeleted"));
                object.put("forum", newThread.getString("forum"));
                object.put("title", newThread.getString("title"));
                object.put("isClosed", newThread.getBoolean("isClosed"));
                object.put("user", newThread.getString("user"));
                object.put("date", newThread.getDate("date"));
                object.put("message", newThread.getString("message"));
                object.put("slug", newThread.getString("slug"));

                System.out.println("Delicious string");

                wrappedObject.put("code", 0);
                wrappedObject.put("response", object);
                System.out.printf(wrappedObject.toString());
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


    @RequestMapping("db/api/thread/details")
    public String threadDetails(@RequestBody(required = false) String payload, @RequestParam Integer thread, @RequestParam("related") ArrayList<String> related){
        Connection conn = null;
        JSONArray relatedArray;
        ArrayList<String> relatedList = new ArrayList<>();
        if (thread == null) {
            try {
                JSONObject object = new JSONObject(payload);
                thread = object.getInt("thread");
                relatedArray = object.getJSONArray("related");
                for (int index = 0; index < relatedArray.length(); ++index) {
                    relatedList.add(relatedArray.getString(index));
                }
//                    related = relatedArray.getString(0);
//                } catch (Exception e) {
////                    related = null;
//                }
            } catch (Exception e) {
                JSONObject error = new JSONObject();
                error.put("code", 3);
                error.put("response", "Not null constraints failed");
                return error.toString();
            }
        } else {
            if (related != null) {
                relatedList = related;
            }
        }

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            JSONObject returnObject = getThreadInfo_forum(conn, thread, relatedList);
            return returnObject.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "";
    }

    private JSONObject getThreadInfo_forum(Connection conn, Integer thread, ArrayList<String> related) {
        try {
            JSONObject object = new JSONObject();
            JSONObject object_in = DbUtils.getThreadInfo(conn, thread, related);
            object.put("code", 0);
            object.put("response", object_in);
            return object;
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
