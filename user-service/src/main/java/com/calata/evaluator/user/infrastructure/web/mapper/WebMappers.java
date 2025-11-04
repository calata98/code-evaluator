package com.calata.evaluator.user.infrastructure.web.mapper;

import com.calata.evaluator.user.domain.model.Role;
import com.calata.evaluator.user.domain.model.User;
import com.calata.evaluator.user.infrastructure.web.dto.UserResponse;

public final class WebMappers {
    private WebMappers(){}

    public static Role toRole(String s){
        return s == null ? Role.USER : Role.valueOf(s.toUpperCase());
    }

    public static UserResponse toUserResponse(User u){
        var dto = new UserResponse();
        dto.id = u.getId();
        dto.email = u.getEmail();
        dto.role = u.getRole().name();
        dto.createdAt = u.getCreatedAt().toString();
        return dto;
    }
}
