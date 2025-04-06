package com.esprit.spring.PiProject.Services;

import com.esprit.spring.PiProject.entities.Offer;
import com.esprit.spring.PiProject.entities.User;

public interface IUserService {
    User AddUser(User user);
    User EditUser(User user);
    void DeleteUser(int id);
}
