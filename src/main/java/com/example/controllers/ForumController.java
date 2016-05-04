package com.example.controllers;

import com.example.CopyPasteWrappingDetails;
import com.example.DbUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by ann on 03.04.16.
 */
@RestController
public class ForumController {
    private final String CREATE_QUERY = "INSERT INTO forums (user, name, short_name) VALUES (?, ?, ?);";
    private final String GET_USER_QUERY = "SELECT * FROM users WHERE email = ?;";
    private final String GET_FORUM_QUERY = "SELECT * FROM forums WHERE short_name = ?;";
    private final String DETAILS_TABLE_FORUM = "SELECT * FROM forums RIGHT JOIN user ON forum.user = user.email WHERE user = ? AND forum = ?;";

    @RequestMapping("db/api/forum/create")
    public String createForum(@RequestBody String payload) {
        JSONObject object = new JSONObject(payload);
        String name, short_name, user;
        try {
            name = StringEscapeUtils.unescapeJava(object.getString("name"));
            System.out.println("VERY VERY BIG " + name);
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
            statement.setString(2, StringEscapeUtils.escapeJava(name));
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


    @RequestMapping("db/api/forum/details")
    public String forumDetails(@RequestBody(required=false) String payload, @RequestParam String forum, @RequestParam String related) {
        Connection conn = null;
        //JSONObject object = new JSONObject(payload);
        //String forum, related;
        JSONArray relatedArray;
        if (forum == null) {
            try {
                JSONObject object = new JSONObject(payload);
                forum = object.getString("forum");
                try {
                    relatedArray = object.getJSONArray("related");
                    related = relatedArray.getString(0);
                } catch (Exception e) {
                    related = null;
                }
            } catch (Exception e) {
                JSONObject error = new JSONObject();
                error.put("code", 3);
                error.put("response", "Not null constraints failed");
                return error.toString();
            }
        }

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            JSONObject returnObject = getForumInfo_forum(conn, forum, related);
            return returnObject.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "";
    }

    private JSONObject getForumInfo_forum(Connection conn, String forum, String related) {
        try {
            JSONObject object = new JSONObject();
            JSONObject object_in = DbUtils.getForumInfo(conn, forum, related);
            object.put("code", 0);
            object.put("response", object_in);
            return object;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    @RequestMapping("db/api/forum/listPosts")
    public String listPosts(@RequestBody(required = false) String payload,
                            @RequestParam String forum,
                            @RequestParam(required = false) String since,
                            @RequestParam(required = false) String order,
                            @RequestParam(required = false) Integer limit,
                            @RequestParam(value = "related", required = false) ArrayList<String> related) {
        Connection conn = null;
        JSONArray relatedArray;
        ArrayList<String> relatedList = new ArrayList<>();
        if (forum == null) {
            try {
                JSONObject object = new JSONObject(payload);
                forum = object.getString("forum");
                relatedArray = object.getJSONArray("related");
                try {
                    since = object.getString("since");
                } catch (Exception e) {
                    since = "1970-01-01 00:00:01";
                }
                for(int index = 0; index < relatedArray.length(); index++) {
                    relatedList.add(relatedArray.getString(index));
                }
                try {
                    order = object.getString("order");
                } catch (Exception e) {
                    order = "desc";
                }
                limit = object.getInt("limit");
                if (limit == 0) {
                    limit = 1000000;
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
            if (order == null) {
                order = "desc";
            }
            if (limit == null) {
                limit = 1000000;
            }
            if (since == null) {
                since = "1970-01-01 00:00:01";
            }
        }
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            JSONObject object = new JSONObject();
            JSONArray object_in = DbUtils.getForumPostsList(conn, relatedList, forum, order, since, limit);
            object.put("code", 0);
            object.put("response", object_in);
//
            System.out.println(object.toString());
            return object.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        }
    }



    @RequestMapping("db/api/forum/listThreads")
    public String listThreads(@RequestBody(required = false) String payload,
                            @RequestParam String forum,
                            @RequestParam(required = false) String since,
                            @RequestParam(required = false) String order,
                            @RequestParam(required = false) Integer limit,
                            @RequestParam(value = "related", required = false) ArrayList<String> related) {
        Connection conn = null;
        JSONArray relatedArray;
        ArrayList<String> relatedList = new ArrayList<>();
        if (forum == null) {
            try {
                JSONObject object = new JSONObject(payload);
                forum = object.getString("forum");
                relatedArray = object.getJSONArray("related");
                try {
                    since = object.getString("since");
                } catch (Exception e) {
                    since = "1970-01-01 00:00:01";
                }
                for(int index = 0; index < relatedArray.length(); index++) {
                    relatedList.add(relatedArray.getString(index));
                }
                try {
                    order = object.getString("order");
                } catch (Exception e) {
                    order = "desc";
                }
                limit = object.getInt("limit");
                if (limit == 0) {
                    limit = 1000000;
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
            if (order == null) {
                order = "desc";
            }
            if (limit == null) {
                limit = 1000000;
            }
            if (since == null) {
                since = "1970-01-01 00:00:01";
            }
        }
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            JSONObject object = new JSONObject();
            JSONArray object_in = DbUtils.getForumThreadssList(conn, relatedList, forum, order, since, limit);
            object.put("code", 0);
            object.put("response", object_in);
//
            System.out.println(object.toString());
            return object.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        }
    }




    @RequestMapping("db/api/forum/listUsers")
    public String listUserss(@RequestBody(required = false) String payload,
                              @RequestParam String forum,
                              @RequestParam(required = false) String since,
                              @RequestParam(required = false) String order,
                              @RequestParam(required = false) Integer limit) {
        Connection conn = null;
        if (forum == null) {
            try {
                JSONObject object = new JSONObject(payload);
                forum = object.getString("forum");
                try {
                    since = object.getString("since");
                } catch (Exception e) {
                    since = "1970-01-01 00:00:01";
                }
                try {
                    order = object.getString("order");
                } catch (Exception e) {
                    order = "desc";
                }
                limit = object.getInt("limit");
                if (limit == 0) {
                    limit = 1000000;
                }
            } catch (Exception e) {
                JSONObject error = new JSONObject();
                error.put("code", 3);
                error.put("response", "Not null constraints failed");
                return error.toString();
            }
        } else{
            if (order == null) {
                order = "desc";
            }
            if (limit == null) {
                limit = 1000000;
            }
            if (since == null) {
                since = "1970-01-01 00:00:01";
            }
        }
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            JSONObject object = new JSONObject();
            JSONArray object_in = DbUtils.getForumUsersList(conn, forum, order, since, limit);
            object.put("code", 0);
            object.put("response", object_in);
//
            System.out.println(object.toString());
            return object.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        }
    }

}
