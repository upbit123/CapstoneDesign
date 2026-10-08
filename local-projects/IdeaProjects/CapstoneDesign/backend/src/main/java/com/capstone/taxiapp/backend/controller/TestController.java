package com.capstone.taxiapp.backend.controller;

import com.capstone.taxiapp.backend.entity.User;
import com.capstone.taxiapp.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final UserRepository userRepository;

    public TestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/db-test")
    public String dbTest() {
        User user = new User("test01", "홍길동");
        userRepository.save(user);
        return "DB 저장 성공";
    }
}