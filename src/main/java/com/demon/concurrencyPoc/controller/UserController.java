package com.demon.concurrencyPoc.controller;

import com.demon.concurrencyPoc.dto.UserUpdateRequest;
import com.demon.concurrencyPoc.entity.User;
import com.demon.concurrencyPoc.service.faultTolerance.ResilientUserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final ResilientUserFacade userFacade;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userFacade.getUser(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(userFacade.updateUser(id, request));
    }
}