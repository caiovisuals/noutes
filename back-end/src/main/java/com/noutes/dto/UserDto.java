package com.noutes.dto;

import com.noutes.entity.User;

import java.util.UUID;

public record UserDto(UUID id, String name, String email, String avatar) {
    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getAvatar());
    }
}