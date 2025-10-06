package net.awords.agriecombackend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public static class LoginRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String password;
    }

    public static class RegisterRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String password;
    }

    public static class UserInfo {
        public Long id;
        public String username;
        public String[] roles;
    }
}
