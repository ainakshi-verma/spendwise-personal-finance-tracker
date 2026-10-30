package com.spendwise.dto;

import com.spendwise.entity.User;

public class UserProfileResponse {

    private Long id;
    private String name;
    private String email;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public static UserProfileResponse fromEntity(User user) {
        return new UserProfileResponse(user.getId(), user.getName(), user.getEmail());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
