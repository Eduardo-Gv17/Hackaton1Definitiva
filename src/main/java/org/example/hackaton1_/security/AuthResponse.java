package org.example.hackaton1_.security;

public class AuthResponse {
    private String token;
    private long expiresIn;
    private String role;
    private String branch;

    public AuthResponse(String token, long expiresIn, String role, String branch) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.role = role;
        this.branch = branch;
    }


    public String getToken() { return token; }
    public long getExpiresIn() { return expiresIn; }
    public String getRole() { return role; }
    public String getBranch() { return branch; }
}