package com.smartalarm.repository;

import com.smartalarm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOpenId(String openId);
    Optional<User> findByUnionId(String unionId);
    boolean existsByOpenId(String openId);
}
