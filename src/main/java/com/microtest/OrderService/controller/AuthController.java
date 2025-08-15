package com.microtest.OrderService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("testapp")
    public ResponseEntity<String> testApp() {
        return ResponseEntity.ok("Your application is working");
    }

}
