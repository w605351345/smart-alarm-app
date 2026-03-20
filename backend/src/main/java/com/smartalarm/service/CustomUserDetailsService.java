package com.smartalarm.service;

import com.smartalarm.model.User;
import com.smartalarm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findById(Long.parseLong(userId));
        
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found with id: " + userId);
        }
        
        User user = userOpt.get();
        
        return new org.springframework.security.core.userdetails.User(
            user.getId().toString(),
            "",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    public UserDetails loadUserByOpenId(String openId) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByOpenId(openId);
        
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found with openId: " + openId);
        }
        
        User user = userOpt.get();
        
        return new org.springframework.security.core.userdetails.User(
            user.getId().toString(),
            "",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
