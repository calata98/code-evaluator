package com.calata.evaluator.user.infrastructure.repo.mapper;

import com.calata.evaluator.user.domain.model.User;
import com.calata.evaluator.user.infrastructure.repo.UserDocument;

public final class UserPersistenceMapper {
    private UserPersistenceMapper(){}

    public static UserDocument toDoc(User u){
        return new UserDocument(u.getId(), u.getEmail(), u.getPasswordHash(), u.getRole(), u.getCreatedAt());
    }
    public static User toDomain(UserDocument d){
        return new User(d.getId(), d.getEmail(), d.getPasswordHash(), d.getRole(), d.getCreatedAt());
    }
}
