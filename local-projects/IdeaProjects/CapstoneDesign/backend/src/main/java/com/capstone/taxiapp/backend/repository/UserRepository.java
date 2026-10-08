package com.capstone.taxiapp.backend.repository;

import com.capstone.taxiapp.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}