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
        return new UserResponse(u.id(), u.email(), u.role().name(), u.createdAt().toString());
    }
}
