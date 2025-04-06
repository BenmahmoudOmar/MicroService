package com.esprit.spring.PiProject.Repository;

import com.esprit.spring.PiProject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
