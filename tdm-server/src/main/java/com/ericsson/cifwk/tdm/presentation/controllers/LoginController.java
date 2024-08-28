package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.api.model.AuthenticationStatus;
import com.ericsson.cifwk.tdm.api.model.UserCredentials;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private SecurityService securityService;

    @GetMapping
    public ResponseEntity<AuthenticationStatus> status() {
        AuthenticationStatus user = securityService.getCurrentUser();
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<AuthenticationStatus> login(@Valid @RequestBody UserCredentials credentials) {
        AuthenticationStatus status = securityService.login(credentials);
        if (status.isAuthenticated()) {
            return new ResponseEntity<>(status, HttpStatus.OK);
        }
        return new ResponseEntity<>(status, HttpStatus.UNAUTHORIZED);
    }
}
