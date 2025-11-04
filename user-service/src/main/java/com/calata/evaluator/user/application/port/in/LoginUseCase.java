package com.calata.evaluator.user.application.port.in;

import com.calata.evaluator.user.application.command.LoginCommand;
import com.calata.evaluator.user.domain.model.AuthToken;

public interface LoginUseCase {
    AuthToken login(LoginCommand cmd);
}
