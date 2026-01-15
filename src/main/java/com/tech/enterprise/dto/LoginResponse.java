package com.tech.enterprise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for successful login response.
 * Contains admin profile info (without sensitive data like password).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Long adminId;
    private String username;
    private Long tenantId;
    private String tenantName;
    private String tenantSlug;
    private String message;
}
