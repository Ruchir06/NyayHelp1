package com.nyayhelp.userservice.repository;

import com.nyayhelp.userservice.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByAuthUserId(Long authUserId);
    Optional<UserProfile> findByEmail(String email);
       List<UserProfile> findByRoleAndVerificationStatus(String role, String verificationStatus);
}