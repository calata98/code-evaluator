package com.calata.evaluator.user.application.port.out;

import com.calata.evaluator.user.domain.model.User;

public interface UserWriter {
    User save(User user);
}
