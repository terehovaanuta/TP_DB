package com.example.controllers;

import com.example.DbUtils;
import com.example.models.User;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.json.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.ArrayList;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.sym.error;
import static com.sun.xml.internal.ws.api.model.wsdl.WSDLBoundOperation.ANONYMOUS.required;

/**
 * Created by ann on 03.04.16.
 */
@RestController
public class UserController {

    private final String CREATE_QUERY = "INSERT INTO users(email, username, name, about, isAnonymous) VALUES (?, ?, ?, ?, ?);";
    private final String GET_USER_QUERY = "SELECT * FROM users  WHERE email = ?;";
    private final String DETAILS_TABLE_USERS = "SELECT * FROM users WHERE email = ?;";
    private final String UPDATE_USER_QUERY = "UPDATE users SET name = ?, about = ? WHERE email = ?;";


    @RequestMapping("db/api/user/create")
    public String createUser(@RequestBody String payload) {
        JSONObject object = new JSONObject(payload);
        String email, name, username, about;
        Boolean isAnonymous;
        try {
            email = object.getString("email");

        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        }

        try {
            name = object.getString("name");
        } catch (Exception e) {
            name = null;
        }
        try {
            username = object.getString("username");
        } catch (Exception e) {
            username = null;
        }
        try {
            about = object.getString("about");
        } catch (Exception e) {
            about = null;
        }
        try {
            isAnonymous = object.getBoolean("isAnonymous");
        } catch (Exception e) {
            isAnonymous = null;
        }
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            PreparedStatement statement = conn.prepareStatement(CREATE_QUERY);
            statement.setString(1, email);
            statement.setString(2, username);
            statement.setString(3, name);
            statement.setString(4, about);
            if (isAnonymous != null) {
                statement.setBoolean(5, isAnonymous);
            } else {
                statement.setNull(5, Types.BOOLEAN);
            }
            PreparedStatement getStatement = conn.prepareStatement(GET_USER_QUERY);
            getStatement.setString(1, email);
            ResultSet isUserCreated = getStatement.executeQuery();
            if (isUserCreated.next()) {
                JSONObject error = new JSONObject();
                error.put("code", 5);
                error.put("response", "such user exists");
                return error.toString();
            }
            int numberOfRowsAffected = statement.executeUpdate();
            if (numberOfRowsAffected == 0) {
                JSONObject error = new JSONObject();
                error.put("code", 5);
                error.put("response", "such user exists");
                return error.toString();
            } else {
                getStatement = conn.prepareStatement(GET_USER_QUERY);
                getStatement.setString(1, email);
                ResultSet newUser = getStatement.executeQuery();
                if (newUser.next()) {
                    JSONObject wrappedObject = new JSONObject();
                    object = new JSONObject();
                    object.put("id", newUser.getInt("id"));
                    object.put("email", newUser.getString("email"));
                    object.put("username", newUser.getString("username"));
                    object.put("about", newUser.getString("about"));
                    object.put("name", newUser.getString("name"));
                    wrappedObject.put("code", 0);
                    wrappedObject.put("response", object);
                    return wrappedObject.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Hello, world!";
    }

    @RequestMapping("db/api/user/details")
    public String userDetails(@RequestBody(required=false) String payload, @RequestParam String user) {
        Connection conn = null;
//        System.out.println("DETAILS1");
//        System.out.println(user);
        //String email;
        if (user == null) {
            try {
                JSONObject object = new JSONObject(payload);
                user = object.getString("user");
            } catch (Exception e) {
                JSONObject error = new JSONObject();
                error.put("code", 3);
                error.put("response", "Not null constraints failed");
                return error.toString();
            }
        }


        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            JSONObject returnObject = getUserInfo(conn, user);
            return returnObject.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    private JSONObject getUserInfo(Connection conn, String email) {
        try {
//            System.out.println("DETAILS");
//            System.out.println(email);
            JSONObject object = new JSONObject();
            JSONObject object_in = DbUtils.getUserInfo(conn, email);
            object.put("code", 0);
            object.put("response", object_in);
            return object;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    @RequestMapping("db/api/user/updateProfile")
    public String updateUser(@RequestBody String payload) {

        Connection conn = null;
        JSONObject object = new JSONObject(payload);
        String user = null;
        String name = null;
        String about = null;
        about = object.getString("about");
        user = object.getString("user");
        name = object.getString("name");
        if (user == null) {
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        } try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            // Delete thread
            PreparedStatement statement = conn.prepareStatement(UPDATE_USER_QUERY);
            statement.setString(1, name);
            statement.setString(2, about);
            statement.setString(3, user);
            statement.executeUpdate();


            JSONObject answer = new JSONObject();
            answer.put("code", 0);

            answer.put("response", DbUtils.getUserInfo(conn, user));
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

