package com.secureshare.controller;

import com.secureshare.model.VerifyResult;
import org.springframework.web.bind.annotation.*;

@RestController
public class VerifyController {

    // API verify - POST
    @PostMapping("/api/verify")
    public VerifyResult verify() {
        return new VerifyResult(true, "Signature Valid");
    }
}
