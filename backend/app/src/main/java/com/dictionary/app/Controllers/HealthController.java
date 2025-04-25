package com.dictionary.app.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping
    public ResponseEntity<?> checkHealth() {
        return ResponseEntity.ok().body("Server is up and running!");
    }
}
