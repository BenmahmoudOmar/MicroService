package com.esprit.spring.PiProject.Services;

import com.esprit.spring.PiProject.Repository.UserRepository;
import com.esprit.spring.PiProject.entities.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserService implements IUserService{
    UserRepository userRepository;
    @Override
    public User AddUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User EditUser(User user) {
        return userRepository.save(user);
    }


    @Override
    public void DeleteUser(int id) {
        userRepository.deleteById(id);
    }
}
