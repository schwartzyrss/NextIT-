package com.nextit.repository;

import com.nextit.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(int id);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    void save(User user);
    void update(User user);
    void delete(int id);
}
