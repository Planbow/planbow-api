package com.planbow.security.response;


import lombok.Data;

import java.util.List;

@Data
public class TokenInfo {
    private String id;
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private String username;
    private String email;
    private long expireIn;
    private List<String> roles;
}
