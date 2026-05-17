package com.capstone.taxiApp.backend.repository;

import com.capstone.taxiApp.backend.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<University, Long> {
}