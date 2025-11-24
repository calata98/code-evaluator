package com.calata.evaluator.user.infrastructure.repo;

import com.calata.evaluator.user.application.port.out.UserReader;
import com.calata.evaluator.user.application.port.out.UserWriter;
import com.calata.evaluator.user.domain.model.User;
import com.calata.evaluator.user.infrastructure.repo.mapper.UserPersistenceMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserMongoAdapter implements UserReader, UserWriter {

    private final SpringDataUserRepository repo;

    public UserMongoAdapter(SpringDataUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email.toLowerCase()).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findById(String id) {
        return repo.findById(id).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public User save(User user) {
        var saved = repo.save(UserPersistenceMapper.toDoc(user));
        return UserPersistenceMapper.toDomain(saved);
    }
}
