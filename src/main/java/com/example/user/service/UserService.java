package com.example.user.service;

import com.example.user.config.PasswordEncoderConfig;
import com.example.user.model.User;
import com.example.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);

    public UserService(BCryptPasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User details are required.");
        }
        validateEmail(user.getEmail());
        String normalizedEmail = normalizeEmail(user.getEmail());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalStateException("Email is already in use.");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password must not be blank.");
        }
        user.setEmail(normalizedEmail);
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setPassword(user.getPassword());
        User savedUser = userRepository.save(user);
        return savedUser;
    }

/*
    public boolean existsByEmail(String email) {
        return email != null && userRepository.existsByEmailIgnoreCase(normalizeEmail(email));
    }
*/

    public User authenticate(String email, String password) {
        if (email == null || password == null) {
            throw new RuntimeException();
        }

        User user = getUserByEmail(email);
        if (user == null) {
            throw new RuntimeException();
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException();
        }

        return user;
    }

    public User getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email)).orElse(null);
    }

    public User getUserById(Long id) {
        log.debug("User cache miss for id={}; loading user from database", id);
        return userRepository.findById(id).orElseThrow(()->new IllegalArgumentException("User not found."));
    }

    /*public User updateProfile(Long id, UpdateProfileRequest request) {
        User user = this.getUserById(id);
        String previousEmail = user.getEmail();

        if (request == null) {
            throw new IllegalArgumentException("Profile update request is required.");
        }
        if (request.name() != null && request.name().isBlank()) {
            throw new IllegalArgumentException("Name must not be blank.");
        }
        if (request.email() != null) {
            validateEmail(request.email());
        }
        if (request.password() != null && request.password().isBlank()) {
            throw new IllegalArgumentException("Password must not be blank.");
        }
        if (request.email() != null && !normalizeEmail(request.email()).equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmailIgnoreCase(normalizeEmail(request.email()))) {
            throw new IllegalStateException("Email is already in use.");
        }

        if (request.name() != null) {
            user.setName(request.name().trim());
        }
        if (request.email() != null) {
            user.setEmail(normalizeEmail(request.email()));
        }
        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        User updatedUser = userRepository.save(user);
        evictEmailCache(previousEmail);
        evictEmailCache(updatedUser.getEmail());
        return updatedUser;
    }*/

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Email must be a valid email address.");
        }
    }

    public String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

/*    public User updateRole(Long id, UpdateRoleRequest request) {
        if (request == null || request.role() == null || request.role().isBlank()) {
            throw new IllegalArgumentException("Role is required.");
        }
        String role = request.role().trim().toUpperCase();
        if (!role.equals("USER") && !role.equals("ADMIN")) {
            throw new IllegalArgumentException("Role must be USER or ADMIN.");
        }
        User user = this.getUserById(id);
        user.setRole(role);
        User updatedUser = userRepository.save(user);
        evictEmailCache(updatedUser.getEmail());
        return updatedUser;
    }

    private void evictEmailCache(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        Cache cache = cacheManager.getCache("usersByEmail");
        if (cache != null) {
            cache.evict(normalizeEmail(email));
        }
    }*/
}