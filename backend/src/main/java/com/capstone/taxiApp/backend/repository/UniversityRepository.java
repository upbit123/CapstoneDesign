package com.capstone.taxiApp.backend.repository;

import com.capstone.taxiApp.backend.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UniversityRepository extends JpaRepository<University, Long> {
    Optional<University> findBySchoolEmailDomainIgnoreCase(String schoolEmailDomain);
}
