package com.example;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by ann on 31.03.16.
 */
@RestController
public class HelloController {
    @RequestMapping("/")
    public String hello() {
        return "Hello, world!";
    }
}
