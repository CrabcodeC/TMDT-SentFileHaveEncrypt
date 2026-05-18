package com.secureshare.controller;

import com.secureshare.model.VerifyResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerifyController {

    @GetMapping("/verify")
    public VerifyResult verify() {

        return new VerifyResult(
                true,
                "Signature Valid"
        );
    }
}