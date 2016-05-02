package com.example;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by ann on 02.05.16.
 */
public class CopyPasteWrappingDetails {
    private static final String DETAILS_TABLE_USERS = "SELECT * FROM users WHERE email = ?;";
    private static final String GET_FORUM_QUERY = "SELECT * FROM forums WHERE short_name = ?;";
    private static final String GET_POST_QUERY = "SELECT * FROM posts WHERE message = ?;";
    private static final String GET_POST_QUERY_ByID = "SELECT * FROM posts WHERE id = ?;";

    private static  final String GET_THREAD_QUERY = "SELECT * FROM threads WHERE title = ?;";
    private static  final String GET_THREAD_QUERY_BY_ID = "SELECT * FROM threads WHERE id = ?;";


    private static final String GET_FORUM_LIST_POSTS_MAIN = "SELECT * FROM forums LEFT JOIN posts ON forums.short_name = posts.forum ";
    private static final String GET_FORUM_LIST_POSTS_MAIN_CLAUSE = "WHERE posts.forum = ? ";
    private static final String GET_FORUM_LIST_POSTS_ORDER_BY = "ORDER BY posts.date ";
    private static final String GET_FORUM_LIST_POSTS_LIMIT = "LIMIT ? ";
    private static final String GET_COUNT_POSTS = "SELECT COUNT(*) FROM posts WHERE thread = ?";

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

        System.out.println(CURRENT_QUERY);
        PreparedStatement statement = conn.prepareStatement(CURRENT_QUERY);
//        statement.setTimestamp(1, Timestamp.valueOf(since));
        statement.setString(1, forum);
        statement.setInt(2, limit);
//        statement.setInt(4, limit);
        System.out.println("LISNDJKLFKJLDFKJLDF " + limit);
        System.out.println(statement);
        ResultSet resultSet = statement.executeQuery();
        JSONArray resultArray = new JSONArray();
        while (resultSet.next()) {
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
                PreparedStatement statement2 = conn.prepareStatement(GET_THREAD_QUERY_BY_ID);
                statement2.setInt(1, resultSet.getInt("posts.thread"));
                ResultSet resultSet2 = statement2.executeQuery();
                resultSet2.first();
                JSONObject object_in2 = new JSONObject();
                object_in2.put("id", resultSet2.getInt("id"));
                object_in2.put("message", resultSet2.getString("message"));
                object_in2.put("title", StringEscapeUtils.unescapeJava(resultSet2.getString("title")));
                object_in2.put("slug", resultSet2.getString("slug"));
                object_in2.put("isClosed", resultSet2.getBoolean("isClosed"));
                object_in2.put("isDeleted", resultSet2.getBoolean("isDeleted"));

                Timestamp date2 = resultSet2.getTimestamp("date");
                Date parsedDate2 = new Date(date2.getTime());
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate2 = sdf.format(parsedDate2);
                object_in2.put("date", formattedDate2);

                // Flags for related field
                boolean isUserRelated2 = false, isForumRelated2 = false;

//            object_in.put("short_name", resultSet.getString("short_name"));
                if (!isUserRelated) {
                    object_in2.put("user", resultSet2.getString("user"));
                } else {
                    object_in2.put("user", DbUtils.getUserInfo(conn, resultSet.getString("user")));
                }

                if (isForumRelated) {
                    object_in2.put("forum", DbUtils.getForumInfo(conn, resultSet.getString("forum"), null));
                } else {
                    object_in2.put("forum", resultSet2.getString("forum"));
                }


                PreparedStatement statement3 = conn.prepareStatement(GET_COUNT_POSTS);
                statement3.setInt(1, resultSet2.getInt("id"));
                ResultSet resultCount = statement3.executeQuery();
                resultCount.first();
                object_in2.put("posts", resultCount.getInt(1));

                object_in.put("thread", object_in2);
//                object_in.put("thread", DbUtils.getThreadInfo(conn, resultSet.getInt("posts.thread"), new ArrayList<String>()));
            } else {
                object_in.put("thread", resultSet.getInt("posts.thread"));
            }

            if (isForumRelated) {
                object_in.put("forum", DbUtils.getForumInfo(conn, resultSet.getString("posts.forum"), null));
            } else {
                object_in.put("forum", resultSet.getString("posts.forum"));
            }
//            resultArray.put(DbUtils.wrapOnePost(conn, resultSet, related));
            resultArray.put(object_in);
        }
        resultSet.close();
        return resultArray;



    }
}
