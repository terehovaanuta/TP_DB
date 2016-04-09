package com.example.controllers;

import com.example.models.User;
import org.json.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.sym.error;

/**
 * Created by ann on 03.04.16.
 */
@RestController
public class UserController {

    private final String CREATE_QUERY = "INSERT INTO users(email, username, name, about) VALUES (?, ?, ?, ?);";
private final String GET_USER_QUERY = "SELECT * FROM users  WHERE email = ?;";
    @RequestMapping("db/api/user/create")
    public String createUser(@RequestBody String payload) {
//        System.out.println(payload);
//        payload = payload.replaceAll("None", "null");
        JSONObject object = new JSONObject(payload);
        String email, name, username, about;
        try {
            email = object.getString("email");
            name = object.getString("name");
            username = object.getString("username");
            about = object.getString("about");
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("code", 3);
            error.put("response", "Not null constraints failed");
            return error.toString();
        }
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/DB_TP", "user1", "123");
            PreparedStatement statement = conn.prepareStatement(CREATE_QUERY);
            System.out.println(email);
            System.out.println(username);
            System.out.println(name);
            System.out.println(about);
            statement.setString(1, email);
            statement.setString(2, username);
            statement.setString(3, name);
            statement.setString(4, about);
            PreparedStatement getStatement = conn.prepareStatement(GET_USER_QUERY);
            getStatement.setString(1, email);
            ResultSet isUserCreated = getStatement.executeQuery();
            if (isUserCreated.next()) {
                JSONObject error = new JSONObject();
                error.put("code", 5);
                error.put("response", "such user exists");
                return error.toString();
//                return "{code: 5, response: Such user exists}";
            }
            int numberOfRowsAffected = statement.executeUpdate();
            if (numberOfRowsAffected == 0) {
                JSONObject error = new JSONObject();
                error.put("code", 5);
                error.put("response", "such user exists");
                return error.toString();
//                return "{code: 5, response: Such user exists}";
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
//                    User user = new User(id, email, username, name, about);
                    return wrappedObject.toString();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Hello, world!";
    }
}
