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



    private static final String DETAILS_USERS_TABLE_SELECT =
            "(SELECT users.*, GROUP_CONCAT(userThreadSubs.thread) AS subscriptions FROM users " +
            "LEFT JOIN userThreadSubs ON users.email = userThreadSubs.user " +
            "GROUP BY users.id) AS userDetails ";

    private static final String DETAILS_POST_TABLE_SELECT =
            "(SELECT posts.*, SUM(postVotes.likes) AS likes, " +
                    "SUM(postVotes.dislikes) AS dislikes, " +
                    "SUM(postVotes.vote) AS points " +
                    "FROM posts " +
                    "LEFT JOIN postVotes ON postVotes.post = posts.id " +
                    "GROUP BY posts.id) AS postDetails ";

    private static final String DETAILS_TABLE_USERS =
            "SELECT * FROM " + DETAILS_USERS_TABLE_SELECT + " WHERE userDetails.email = ?;";

    private static final String DETAILS_THREADS_TABLE_SELECT =
            "(SELECT threads.*, COUNT(postsOpened.id) AS postsCount, SUM(threadVotes.likes) / COUNT(postsOpened.id) AS likes, " +
                    "SUM(threadVotes.dislikes) / COUNT(postsOpened.id) AS dislikes, " +
                    "SUM(threadVotes.vote) / COUNT(postsOpened.id) AS points " +
                    "FROM threads " +
                    "LEFT JOIN (SELECT * FROM posts WHERE isDeleted = false) AS postsOpened ON postsOpened.thread = threads.id " +
                    "LEFT JOIN threadVotes ON threadVotes.thread = threads.id " +
                    "GROUP BY threads.id) AS threadDetails ";



    private static final String GET_FORUM_QUERY = "SELECT * FROM forums WHERE short_name = ?;";
    private static final String GET_POST_QUERY = "SELECT * FROM posts WHERE message = ?;";
    private static final String GET_POST_QUERY_ByID = "SELECT * FROM " + DETAILS_POST_TABLE_SELECT + " WHERE postDetails.id = ?;";

    private static  final String GET_THREAD_QUERY = "SELECT * FROM threads WHERE title = ?;";
//    private static  final String GET_THREAD_QUERY_BY_ID = "SELECT * FROM threads WHERE id = ?;";
    private static final String GET_THREAD_QUERY_BY_ID = "SELECT * FROM " + DETAILS_THREADS_TABLE_SELECT + "WHERE threadDetails.id = ?";

    private static final String GET_FORUM_LIST_POSTS_MAIN = "SELECT * FROM forums LEFT JOIN " + DETAILS_POST_TABLE_SELECT + " ON forums.short_name = postDetails.forum ";
    private static final String GET_FORUM_LIST_POSTS_MAIN_CLAUSE = "WHERE postDetails.forum = ? AND postDetails.date > ?";
    private static final String GET_FORUM_LIST_POSTS_ORDER_BY = "ORDER BY postDetails.date ";
    private static final String GET_FORUM_LIST_POSTS_LIMIT = "LIMIT ? ";

    private static final String GET_USER_LIST_POSTS_MAIN = "SELECT * FROM users LEFT JOIN " + DETAILS_POST_TABLE_SELECT + " ON users.email = postDetails.user ";
    private static final String GET_USER_LIST_POSTS_MAIN_CLAUSE = "WHERE postDetails.user = ? AND postDetails.date > ?";
    private static final String GET_USER_LIST_POSTS_ORDER_BY = "ORDER BY postDetails.date ";
    private static final String GET_USER_LIST_POSTS_LIMIT = "LIMIT ? ";

    private static final String GET_FORUM_LIST_THREADS_MAIN = "SELECT * FROM forums LEFT JOIN " + DETAILS_THREADS_TABLE_SELECT +  " ON forums.short_name = threadDetails.forum ";
    private static final String GET_FORUM_LIST_THREADS_MAIN_CLAUSE = "WHERE threadDetails.forum = ? AND threadDetails.date > ?";
    private static final String GET_FORUM_LIST_THREADS_ORDER_BY = "ORDER BY threadDetails.date ";
    private static final String GET_FORUM_LIST_THREADS_LIMIT = "LIMIT ? ";


    private static final String GET_FORUM_LIST_USERS = "SELECT userDetails.* FROM posts JOIN "
                                                        + DETAILS_USERS_TABLE_SELECT +
                                                        " ON userDetails.email = posts.user WHERE posts.forum = ? AND posts.date > ? GROUP BY userDetails.id ORDER BY userDetails.name ";
    private static final String GET_FORUM_LIST_USERS_LIMIT = "LIMIT ?";



    private static final String GET_COUNT_POSTS = "SELECT COUNT(*) FROM posts WHERE thread = ? AND isDeleted = 0";

    private static final String FOLLLOW_TABLE_SELECT =
            "(SELECT users.*" +
                    "FROM users " +
                    "LEFT JOIN followers ON users.email = followers.follower " +
                    "LEFT JOIN followers ON users.email = followers.followee ";

    private static final String SELECT_USER_FOLLOWER =
            FOLLLOW_TABLE_SELECT + "WHERE followers.follower = ? ";

    private static final String SELECT_USER_FOLLOWEE =
            FOLLLOW_TABLE_SELECT + "WHERE followers.followee = ? ";


    public static JSONObject getUserInfo(Connection conn, String email) {
        try {
            PreparedStatement statement = conn.prepareStatement(DETAILS_TABLE_USERS);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            resultSet.first();

            return DbUtils.wrapOneUser(conn, resultSet);
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
            System.out.println(statement);
            ResultSet resultSet = statement.executeQuery();
            resultSet.first();
            return DbUtils.wrapOneThread(conn, resultSet, related);
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

    public static JSONArray getUserPostsList(Connection conn,
                                              String user,
                                              String orderBy,
                                              String since,
                                              Integer limit) throws SQLException {
        String CURRENT_QUERY = GET_USER_LIST_POSTS_MAIN
                + GET_USER_LIST_POSTS_MAIN_CLAUSE
                + GET_USER_LIST_POSTS_ORDER_BY
                + " " + orderBy + " "
                + GET_USER_LIST_POSTS_LIMIT
                + ";";
        PreparedStatement statement = conn.prepareStatement(CURRENT_QUERY);
//        statement.setTimestamp(1, Timestamp.valueOf(since));
        statement.setString(1, user);
        statement.setInt(3, limit);
        statement.setTimestamp(2, Timestamp.valueOf(since));
//        statement.setInt(4, limit);
        System.out.println("LISNDJKLFKJLDFKJLDF " + limit);
        System.out.println(statement);
        ResultSet resultSet = statement.executeQuery();
        JSONArray resultArray = new JSONArray();
        while (resultSet.next()) {
            resultArray.put(DbUtils.wrapOnePost(conn, resultSet, new ArrayList<String>()));
        }
        resultSet.close();
        return resultArray;

//        statement
    }



    public static JSONArray getForumThreadssList(Connection conn,
                                              ArrayList<String> related,
                                              String forum,
                                              String orderBy,
                                              String since,
                                              Integer limit) throws SQLException {
        String CURRENT_QUERY = GET_FORUM_LIST_THREADS_MAIN
                + GET_FORUM_LIST_THREADS_MAIN_CLAUSE
                + GET_FORUM_LIST_THREADS_ORDER_BY
                + " " + orderBy + " "
                + GET_FORUM_LIST_THREADS_LIMIT
                + ";";
        PreparedStatement statement = conn.prepareStatement(CURRENT_QUERY);
//        statement.setTimestamp(1, Timestamp.valueOf(since));
        statement.setString(1, forum);
        statement.setInt(3, limit);
        statement.setTimestamp(2, Timestamp.valueOf(since));
        System.out.println(statement);
//        statement.setInt(4, limit);
        System.out.println("LISNDJKLFKJLDFKJLDF " + limit);
        System.out.println(statement);
        ResultSet resultSet = statement.executeQuery();
        JSONArray resultArray = new JSONArray();
        while (resultSet.next()) {
            resultArray.put(DbUtils.wrapOneThread(conn, resultSet, related));
        }
        resultSet.close();
        return resultArray;

//        statement
    }

    public static JSONArray getForumUsersList(Connection conn,
                                                 String forum,
                                                 String orderBy,
                                                 String since,
                                                 Integer limit) throws SQLException {
        String CURRENT_QUERY = GET_FORUM_LIST_USERS
                + " " + orderBy + " "
                + GET_FORUM_LIST_USERS_LIMIT
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
            resultArray.put(DbUtils.wrapOneUser(conn, resultSet));
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

            Timestamp date = resultSet.getTimestamp("postDetails.date");
            Date parsedDate = new Date(date.getTime());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(parsedDate);
            object_in.put("date", formattedDate);

//            object_in.put("dislikes", resultSet.getInt("dislikes"));
//            object_in.put("forum", resultSet.getString("forum"));
            object_in.put("id", resultSet.getInt("postDetails.id"));
            object_in.put("isApproved", resultSet.getBoolean("postDetails.isApproved"));
            object_in.put("isDeleted", resultSet.getBoolean("postDetails.isDeleted"));
            object_in.put("isEdited", resultSet.getBoolean("postDetails.isEdited"));
            object_in.put("isHighlighted", resultSet.getBoolean("postDetails.isHighlighted"));
            object_in.put("isSpam", resultSet.getBoolean("postDetails.isSpam"));
//            object_in.put("likes", resultSet.getInt("likes"));
            object_in.put("message", resultSet.getString("postDetails.message"));
            Integer parent = resultSet.getInt("postDetails.parent");
            if (parent == 0) {
                object_in.put("parent", JSONObject.NULL);
            } else {
                object_in.put("parent", resultSet.getInt("postDetails.parent"));
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
                object_in.put("user", resultSet.getString("postDetails.user"));
            } else {
                object_in.put("user", DbUtils.getUserInfo(conn, resultSet.getString("postDetails.user")));
            }


            if (isThreadRelated) {
                object_in.put("thread", DbUtils.getThreadInfo(conn, resultSet.getInt("postDetails.thread"), new ArrayList<String>()));
            } else {
                object_in.put("thread", resultSet.getInt("postDetails.thread"));
            }

            if (isForumRelated) {
                object_in.put("forum", DbUtils.getForumInfo(conn, resultSet.getString("postDetails.forum"), null));
            } else {
                object_in.put("forum", resultSet.getString("postDetails.forum"));
            }
            object_in.put("likes", resultSet.getInt("postDetails.likes"));
            object_in.put("dislikes", resultSet.getInt("postDetails.dislikes"));
            object_in.put("points", resultSet.getInt("postDetails.points"));
            System.out.println("ISTHREADRELATED " +  isThreadRelated);
            System.out.println(object_in.toString());
            return object_in;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public static JSONObject wrapOneThread(Connection conn,
                                           ResultSet resultSet,
                                           ArrayList<String> related) {
        try {
            JSONObject object = new JSONObject();
            JSONObject object_in = new JSONObject();
            JSONObject object_in_user = new JSONObject();
            object_in.put("id", resultSet.getInt("threadDetails.id"));
            object_in.put("message", resultSet.getString("threadDetails.message"));
            object_in.put("title", StringEscapeUtils.unescapeJava(resultSet.getString("threadDetails.title")));
            object_in.put("slug", resultSet.getString("threadDetails.slug"));
            object_in.put("isClosed", resultSet.getBoolean("threadDetails.isClosed"));
            object_in.put("isDeleted", resultSet.getBoolean("threadDetails.isDeleted"));

            Timestamp date = resultSet.getTimestamp("threadDetails.date");
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
                object_in.put("user", resultSet.getString("threadDetails.user"));
            } else {
                object_in.put("user", DbUtils.getUserInfo(conn, resultSet.getString("threadDetails.user")));
            }

            if (isForumRelated) {
                object_in.put("forum", DbUtils.getForumInfo(conn, resultSet.getString("threadDetails.forum"), null));
            } else {
                object_in.put("forum", resultSet.getString("threadDetails.forum"));
            }
//            PreparedStatement statement3 = conn.prepareStatement(GET_COUNT_POSTS);
//            statement3.setInt(1, resultSet.getInt("threads.id"));
//            ResultSet resultCount = statement3.executeQuery();
//            resultCount.first();
            int posts = resultSet.getInt("threadDetails.postsCount");
            int likes = resultSet.getInt("threadDetails.likes");
            int dislikes = resultSet.getInt("threadDetails.dislikes");
            int points = resultSet.getInt("threadDetails.points");
//            if (posts != 0) {
//                likes /= posts;
//                dislikes /= posts;
//                points /= posts;
//            }
            object_in.put("posts", posts);
            object_in.put("likes", likes);
            object_in.put("dislikes", dislikes);
            object_in.put("points", points);
            return object_in;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static JSONObject wrapOneUser(Connection conn, ResultSet resultSet) {
        try {
            JSONObject object_in = new JSONObject();
            String about = resultSet.getString("userDetails.about");
            if (about != null) {
                object_in.put("about", about);
            } else {
                object_in.put("about", JSONObject.NULL);
            }
            object_in.put("email", resultSet.getString("userDetails.email"));
            //object_in.put("followers", );
            //object_in.put("following", );
            object_in.put("id", resultSet.getInt("userDetails.id"));
            object_in.put("isAnonymous", resultSet.getBoolean("userDetails.isAnonymous"));
            String name = resultSet.getString("userDetails.name");

            if (name != null) {
                object_in.put("name", name);
            } else  {
                object_in.put("name", JSONObject.NULL);
            }
            //object_in.put("subscriptions", );
            String username = resultSet.getString("userDetails.username");

            object_in.put("subscriptions", parseSubscriptions(resultSet.getString("subscriptions")));
            if (username != null){
                object_in.put("username", username);
            } else {
                object_in.put("username", JSONObject.NULL);
            }

            return object_in;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    private static JSONArray parseSubscriptions(String subscriptions) {
        JSONArray result = new JSONArray();
        if (subscriptions == null) {
            return result;
        }
        String[] parsedArray = subscriptions.split(",");
        for (String subscription : parsedArray) {
            result.put(Integer.parseInt(subscription));
        }
        return result;
    }

}