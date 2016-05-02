package com.example;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.transform.Result;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


/**
 * Created by ann on 13.04.16.
 */
public class DbUtils {

    private static final String DETAILS_TABLE_USERS = "SELECT * FROM users WHERE email = ?;";
    private static final String GET_FORUM_QUERY = "SELECT * FROM forums WHERE short_name = ?;";
    private static final String GET_POST_QUERY = "SELECT * FROM posts WHERE message = ?;";
    private static final String GET_POST_QUERY_ByID = "SELECT * FROM posts WHERE id = ?;";

    private static  final String GET_THREAD_QUERY = "SELECT * FROM threads WHERE title = ?;";
    private static  final String GET_THREAD_QUERY_BY_ID = "SELECT * FROM threads WHERE id = ?;";


    private static final String GET_FORUM_LIST_POSTS_MAIN = "SELECT * FROM forums LEFT JOIN posts ON forums.short_name = posts.forum ";
    private static final String GET_FORUM_LIST_POSTS_MAIN_CLAUSE = "WHERE posts.forum = ? AND posts.date > ?";
    private static final String GET_FORUM_LIST_POSTS_ORDER_BY = "ORDER BY posts.date ";
    private static final String GET_FORUM_LIST_POSTS_LIMIT = "LIMIT ? ";

    private static final String GET_COUNT_POSTS = "SELECT COUNT(*) FROM posts WHERE thread = ? AND isDeleted = 0";

    public static JSONObject getUserInfo(Connection conn, String email) {
        try {
            PreparedStatement statement = conn.prepareStatement(DETAILS_TABLE_USERS);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            resultSet.first();
            JSONObject object = new JSONObject();
            JSONObject object_in = new JSONObject();
            String about = resultSet.getString("about");
            if (about != null) {
                object_in.put("about", about);
            } else {
                object_in.put("about", JSONObject.NULL);
            }
            object_in.put("email", resultSet.getString("email"));
            //object_in.put("followers", );
            //object_in.put("following", );
            object_in.put("id", resultSet.getInt("id"));
            object_in.put("isAnonymous", resultSet.getBoolean("isAnonymous"));
            String name = resultSet.getString("name");
            if (name != null) {
                object_in.put("name", name);
            } else  {
                object_in.put("name", JSONObject.NULL);
            }
            //object_in.put("subscriptions", );
            String username = resultSet.getString("username");
            if (username != null){
                object_in.put("username", username);
            } else {
                object_in.put("username", JSONObject.NULL);
            }

            // object_in.put("username", resultSet.getString("username"));
            System.out.println("DETAILS");
            System.out.println(object_in.toString());
            return object_in;
            //object.put("code", 0);
            //object.put("response", object_in);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static JSONObject getForumInfo(Connection conn, String forum, String related) {
        try {
            PreparedStatement statement = conn.prepareStatement(GET_FORUM_QUERY);
            // GET_FORUM_QUERY
            statement.setString(1, forum);
            ResultSet resultSet = statement.executeQuery();
            resultSet.first();
            JSONObject object = new JSONObject();
            JSONObject object_in = new JSONObject();
            JSONObject object_in_user = new JSONObject();
            object_in.put("id", resultSet.getInt("id"));
            object_in.put("name", StringEscapeUtils.unescapeJava(resultSet.getString("name")));
            object_in.put("short_name", resultSet.getString("short_name"));
            if (related == null) {
                object_in.put("user", resultSet.getString("user"));
            } else {
                object_in.put("user", DbUtils.getUserInfo(conn, resultSet.getString("user")));
            }
            return object_in;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }


    public static JSONObject getThreadInfo(Connection conn, Integer thread, ArrayList<String> related) {
        try {
            PreparedStatement statement = conn.prepareStatement(GET_THREAD_QUERY_BY_ID);
            statement.setInt(1, thread);
            ResultSet resultSet = statement.executeQuery();
            resultSet.first();
            JSONObject object = new JSONObject();
            JSONObject object_in = new JSONObject();
            JSONObject object_in_user = new JSONObject();
            object_in.put("id", resultSet.getInt("id"));
            object_in.put("message", resultSet.getString("message"));
            object_in.put("title", StringEscapeUtils.unescapeJava(resultSet.getString("title")));
            object_in.put("slug", resultSet.getString("slug"));
            object_in.put("isClosed", resultSet.getBoolean("isClosed"));
            object_in.put("isDeleted", resultSet.getBoolean("isDeleted"));

            Timestamp date = resultSet.getTimestamp("date");
            Date parsedDate = new Date(date.getTime());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(parsedDate);
            object_in.put("date", formattedDate);

            // Flags for related field
            boolean isUserRelated = false, isForumRelated = false;
            for (int index = 0; index < related.size(); ++index) {
                if (related.get(index).equals("user")) {
                    isUserRelated = true;
                }
                if (related.get(index).equals("forum")) {
                    isForumRelated = true;
                }
            }
//            object_in.put("short_name", resultSet.getString("short_name"));
            if (!isUserRelated) {
                object_in.put("user", resultSet.getString("user"));
            } else {
                object_in.put("user", DbUtils.getUserInfo(conn, resultSet.getString("user")));
            }

            if (isForumRelated) {
                object_in.put("forum", DbUtils.getForumInfo(conn, resultSet.getString("forum"), null));
            } else {
                object_in.put("forum", resultSet.getString("forum"));
            }
            PreparedStatement statement3 = conn.prepareStatement(GET_COUNT_POSTS);
            statement3.setInt(1, thread);
            ResultSet resultCount = statement3.executeQuery();
            resultCount.first();
            object_in.put("posts", resultCount.getInt(1));
            return object_in;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static JSONObject getPostInfo(Connection conn, Integer post, ArrayList<String> related) {
        try {
            PreparedStatement statement = conn.prepareStatement(GET_POST_QUERY_ByID);
            statement.setInt(1, post);
            ResultSet resultSet = statement.executeQuery();
            resultSet.first();
            return DbUtils.wrapOnePost(conn, resultSet, related);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static JSONArray getForumPostsList(Connection conn,
                                              ArrayList<String> related,
                                              String forum,
                                              String orderBy,
                                              String since,
                                              Integer limit) throws SQLException {
        String CURRENT_QUERY = GET_FORUM_LIST_POSTS_MAIN
                + GET_FORUM_LIST_POSTS_MAIN_CLAUSE
                + GET_FORUM_LIST_POSTS_ORDER_BY
                + " " + orderBy + " "
                + GET_FORUM_LIST_POSTS_LIMIT
                + ";";
        PreparedStatement statement = conn.prepareStatement(CURRENT_QUERY);
//        statement.setTimestamp(1, Timestamp.valueOf(since));
        statement.setString(1, forum);
        statement.setInt(3, limit);
        statement.setTimestamp(2, Timestamp.valueOf(since));
//        statement.setInt(4, limit);
        System.out.println("LISNDJKLFKJLDFKJLDF " + limit);
        System.out.println(statement);
        ResultSet resultSet = statement.executeQuery();
        JSONArray resultArray = new JSONArray();
        while (resultSet.next()) {
            resultArray.put(DbUtils.wrapOnePost(conn, resultSet, related));
        }
        resultSet.close();
        return resultArray;

//        statement


    }

    public static JSONObject wrapOnePost(Connection conn, ResultSet resultSet, ArrayList<String> related) {
        try {
            JSONObject object = new JSONObject();
            JSONObject object_in = new JSONObject();
            JSONObject object_in_user = new JSONObject();

            Timestamp date = resultSet.getTimestamp("posts.date");
            Date parsedDate = new Date(date.getTime());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(parsedDate);
            object_in.put("date", formattedDate);

//            object_in.put("dislikes", resultSet.getInt("dislikes"));
//            object_in.put("forum", resultSet.getString("forum"));
            object_in.put("id", resultSet.getInt("posts.id"));
            object_in.put("isApproved", resultSet.getBoolean("posts.isApproved"));
            object_in.put("isDeleted", resultSet.getBoolean("posts.isDeleted"));
            object_in.put("isEdited", resultSet.getBoolean("posts.isEdited"));
            object_in.put("isHighlighted", resultSet.getBoolean("posts.isHighlighted"));
            object_in.put("isSpam", resultSet.getBoolean("posts.isSpam"));
//            object_in.put("likes", resultSet.getInt("likes"));
            object_in.put("message", resultSet.getString("posts.message"));
            Integer parent = resultSet.getInt("posts.parent");
            if (parent == 0) {
                object_in.put("parent", JSONObject.NULL);
            } else {
                object_in.put("parent", resultSet.getInt("posts.parent"));
            }
//                object_in.put("parent", resultSet.getInt("parent"));
//            }
//            object_in.put("point", resultSet.getInt("point"));
//            object_in.put("thread", resultSet.getInt("thread"));
//            object_in.put("user", resultSet.getString("user"));


            boolean isUserRelated = false, isForumRelated = false, isThreadRelated = false;
            for (int index = 0; index < related.size(); ++index) {
                if (related.get(index).equals("user")) {
                    isUserRelated = true;
                }
                if (related.get(index).equals("thread")) {
                    isThreadRelated = true;
                }
                if (related.get(index).equals("forum")) {
                    isForumRelated = true;
                }
            }

            if (!isUserRelated) {
                object_in.put("user", resultSet.getString("posts.user"));
            } else {
                object_in.put("user", DbUtils.getUserInfo(conn, resultSet.getString("posts.user")));
            }


            if (isThreadRelated) {
                object_in.put("thread", DbUtils.getThreadInfo(conn, resultSet.getInt("posts.thread"), new ArrayList<String>()));
            } else {
                object_in.put("thread", resultSet.getInt("posts.thread"));
            }

            if (isForumRelated) {
                object_in.put("forum", DbUtils.getForumInfo(conn, resultSet.getString("posts.forum"), null));
            } else {
                object_in.put("forum", resultSet.getString("posts.forum"));
            }
            System.out.println("ISTHREADRELATED " +  isThreadRelated);
            System.out.println(object_in.toString());
            return object_in;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }
}