package com.calata.evaluator.user.application.port.in;

import com.calata.evaluator.user.application.command.LogoutCommand;

public interface LogoutUseCase {
    void logout(LogoutCommand cmd);
}
