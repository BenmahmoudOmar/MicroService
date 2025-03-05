package com.esprit.internify.services;

import com.esprit.internify.entities.User;

import java.util.Optional;

public interface IUserService {
    Optional<User> getUserById(Long id);
    Optional<User> getUserByEmail(String email);
}
