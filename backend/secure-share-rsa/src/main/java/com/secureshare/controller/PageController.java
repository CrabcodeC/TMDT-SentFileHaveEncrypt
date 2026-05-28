package com.secureshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/send")
    public String sendPage() {
        return "send";
    }

    @GetMapping("/inbox")
    public String inboxPage() {
        return "inbox";
    }

    @GetMapping("/verify")
    public String verifyPage() {
        return "verify";
    }

    @GetMapping("/createkey")
    public String createKeyPage() {
        return "createkey";
    }
}
