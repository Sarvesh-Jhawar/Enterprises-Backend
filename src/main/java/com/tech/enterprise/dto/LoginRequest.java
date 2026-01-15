package com.tech.enterprise.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login request body.
 */
@Data
@NoArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}
