package com.gametime.Backend.repository;

import com.gametime.Backend.entity.User;
import com.gametime.Backend.entity.User.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStatus(UserStatus status);

    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}
