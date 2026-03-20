package com.smartalarm.repository;

import com.smartalarm.model.WechatUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WechatUserRepository extends JpaRepository<WechatUser, Long> {
    Optional<WechatUser> findByOpenId(String openId);
    Optional<WechatUser> findByUnionId(String unionId);
    boolean existsByOpenId(String openId);
}
