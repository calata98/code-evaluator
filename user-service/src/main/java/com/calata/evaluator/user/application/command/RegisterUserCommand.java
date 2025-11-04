package com.calata.evaluator.user.application.command;

import com.calata.evaluator.user.domain.model.Role;

public record RegisterUserCommand(String email, String password, Role role) {}
