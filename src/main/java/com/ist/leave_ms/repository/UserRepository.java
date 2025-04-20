package com.ist.leave_ms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ist.leave_ms.model.User;

public interface UserRepository extends JpaRepository<User, Long>
{
    boolean existsByEmail(String email);    
    Optional<User> findByEmail(String email);
}
