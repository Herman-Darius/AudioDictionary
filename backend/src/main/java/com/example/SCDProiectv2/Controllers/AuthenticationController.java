package com.example.SCDProiectv2.Controllers;

import com.example.SCDProiectv2.Models.Courier;
import com.example.SCDProiectv2.Services.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class AuthenticationController {
    private final AuthenticationService authenticationService;


    @PostMapping("register")
    public ResponseEntity<?> register(@RequestBody Courier request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody Courier request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

}
