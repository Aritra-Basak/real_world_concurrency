package com.demon.concurrencyPoc.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String email;
    private String phone;
}