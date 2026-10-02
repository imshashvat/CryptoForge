package com.nietproject.cryptoforge.service;

import com.nietproject.cryptoforge.model.User;
import com.nietproject.cryptoforge.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Username or email cannot be empty");
        }
        String clean = username.trim();
        return userRepository.findByUsername(clean)
                .or(() -> userRepository.findByUsername(clean.toLowerCase()))
                .or(() -> userRepository.findByEmail(clean))
                .or(() -> userRepository.findByEmail(clean.toLowerCase()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
