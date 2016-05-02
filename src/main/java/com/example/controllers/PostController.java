package com.example.controllers;

import com.example.DbUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


/**
 * Created by ann on 07.04.16.
 */

@RestController
public class PostController {
    private final String CREATE_QUERY = "INSERT INTO posts (parent, isApproved, isHighlighted, isEdited, isSpam, isDeleted, date, thread, message, user, forum) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private final String GET_USER_QUERY = "SELECT * FROM users WHERE email = ?;";
    private final String GET_FORUM_QUERY = "SELECT * FROM forums WHERE short_name = ?;";

    private final String GET_THREAD_QUERY = "SELECT * FROM threads WHERE id = ?;";
    private final String GET_POST_QUERY = "SELECT * FROM posts WHERE message = ? AND user = ? AND thread = ?;";

    private final String DETALIS_POST_TABLE = "SELECT * FROM posts WHERE post = ?;";

    private final String REMOVE_POST_QUERY = "UPDATE posts SET isDeleted = TRUE WHERE id = ?;";
    private final String RESTORE_POST_QUERY = "UPDATE posts SET isDeleted = FALSE WHERE id = ?;";

    private final String UPDATE_POST_QUERY = "UPDATE posts SET message = ? WHERE id = ?;";

    @RequestMapping("db/api/post/create")
    public String createPost(@RequestBody String payload) {
        JSONObject object = new JSONObject(payload);
//        System.out.println(payload);
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
        } catch (Exception e) {
            parent = null;
        }

        try {
            isApproved = object.getBoolean("isApproved");
        } catch (Exception e) {
            isApproved = null;
        }

        try {
            isHighlighted = object.getBoolean("isHighlighted");
        } catch (Exception e) {
            isHighlighted = null;
        }

        try {
            isEdited = object.getBoolean("isEdited");
        } catch (Exception e) {
            isEdited = null;
        }

        try {
            isSpam = object.getBoolean("isSpam");
        } catch (Exception e) {
            isSpam = null;
        }

        try {
            isDeleted = object.getBoolean("isDeleted");
        } catch (Exception e) {
            isDeleted = null;
        }


        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            PreparedStatement statement = conn.prepareStatement(CREATE_QUERY);

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
//                System.out.println(id);
//                System.out.println(message);
//                System.out.println("HIGHLIGHTED: " + isHighlighted);
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

            statement.setTimestamp(7, Timestamp.valueOf(date));
//            statement.setString(7, date);
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
            getStatement.setInt(3, thread);
            getStatement.setString(2, user);
            ResultSet newPost = getStatement.executeQuery();
            if (newPost.next()) {
                JSONObject wrappedObject = new JSONObject();
                object = new JSONObject();
                object.put("id", newPost.getInt("id"));
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
//                System.out.printf(wrappedObject.toString());
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

    @RequestMapping("db/api/post/details")
    public String postDetails(@RequestBody(required = false) String payload, @RequestParam Integer post,
                              @RequestParam(value = "related", required = false) ArrayList<String> related){
        Connection conn = null;
        JSONArray relatedArray;
        ArrayList<String> relatedList = new ArrayList<>();
        if (post == null) {
            try {
                JSONObject object = new JSONObject(payload);
                post = object.getInt("post");
                relatedArray = object.getJSONArray("related");
                for(int index = 0; index < relatedArray.length(); index++) {
                    relatedList.add(relatedArray.getString(index));
                }
            } catch (Exception e) {
                JSONObject error = new JSONObject();
                error.put("code", 3);
                error.put("response", "Not null constraints failed");
                return error.toString();
            }
        } else{
            if (related != null) {
                relatedList = related;
            }
        }

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            JSONObject returnObject = getPostInfo_forum(conn, post, relatedList);
            System.out.println(returnObject.toString());
            return returnObject.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "";
    }

    private JSONObject getPostInfo_forum(Connection conn, Integer post, ArrayList<String> related) {
        try {
            JSONObject object = new JSONObject();
            JSONObject object_in = DbUtils.getPostInfo(conn, post, related);
            object.put("code", 0);
            object.put("response", object_in);
            return object;
        } catch (Exception e) {
            return new JSONObject();
        }
    }



    @RequestMapping("db/api/post/remove")
    public String removePost(@RequestBody String payload) {
        Connection conn = null;
        JSONObject object = new JSONObject(payload);
        Integer post = null;
        post = object.getInt("post");
        if (post == 0) {
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        } try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            // Delete thread
            PreparedStatement statement = conn.prepareStatement(REMOVE_POST_QUERY);
            statement.setInt(1, post);
            statement.executeUpdate();


            JSONObject answer = new JSONObject();
            answer.put("code", 0);
            JSONObject idObject = new JSONObject();
            idObject.put("post", post);
            answer.put("response", idObject);
            return answer.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        }

    }


    @RequestMapping("db/api/post/restore")
    public String restorePost(@RequestBody String payload) {
        Connection conn = null;
        JSONObject object = new JSONObject(payload);
        Integer post = null;
        post = object.getInt("post");
        if (post == 0) {
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        } try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            // Delete thread
            PreparedStatement statement = conn.prepareStatement(RESTORE_POST_QUERY);
            statement.setInt(1, post);
            statement.executeUpdate();


            JSONObject answer = new JSONObject();
            answer.put("code", 0);
            JSONObject idObject = new JSONObject();
            idObject.put("post", post);
            answer.put("response", idObject);
            return answer.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        }

    }


    @RequestMapping("db/api/post/update")
    public String updatePost(@RequestBody String payload) {
        Connection conn = null;
        JSONObject object = new JSONObject(payload);
        Integer post = null;
        String message = null;
        post = object.getInt("post");
        message = object.getString("message");
        if (post == 0) {
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        } try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            // Delete thread
            PreparedStatement statement = conn.prepareStatement(UPDATE_POST_QUERY);
            statement.setString(1, message);
            statement.setInt(2, post);
            statement.executeUpdate();


            JSONObject answer = new JSONObject();
            answer.put("code", 0);
            JSONObject idObject = new JSONObject();
            idObject.put("post", post);
            answer.put("response", DbUtils.getPostInfo(conn, post, new ArrayList<String>()));
            return answer.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        }

    }

}
