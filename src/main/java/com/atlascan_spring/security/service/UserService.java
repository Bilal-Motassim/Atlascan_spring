package com.atlascan_spring.security.service;


import com.atlascan_spring.security.entities.User;
import com.atlascan_spring.security.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public User getUserIdByEmail(String email) {
        Optional<User> user = repository.findByEmail(email);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new EntityNotFoundException("User with email " + email + " not found");
        }
    }

    public void save(User user) {
        repository.save(user);
    }

}
