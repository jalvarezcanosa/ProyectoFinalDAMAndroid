package com.example.contapp.models;

public class AuthResponse {
    private String token;
    private User user;

    public String getToken() { return token; }
    public User getUser() { return user; }

    public static class User {
        private int id;
        private String username;
        private String email;
        private String telephone;

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getTelephone() { return telephone; }
    }
}
