package com.calata.evaluator.user.application.port.in;

import com.calata.evaluator.user.application.command.RegisterUserCommand;
import com.calata.evaluator.user.domain.model.User;

public interface RegisterUserUseCase {
    User register(RegisterUserCommand cmd);
}
