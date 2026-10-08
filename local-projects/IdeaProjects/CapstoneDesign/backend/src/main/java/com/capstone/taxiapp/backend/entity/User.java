package com.capstone.taxiapp.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
@SequenceGenerator(
        name = "user_seq_gen",
        sequenceName = "user_seq",
        allocationSize = 1
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    private Long id;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String name;

    protected User() {
    }

    public User(String loginId, String name) {
        this.loginId = loginId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getName() {
        return name;
    }
}