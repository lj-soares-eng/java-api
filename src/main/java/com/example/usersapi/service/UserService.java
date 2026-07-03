package com.example.usersapi.service;

import com.example.usersapi.dto.UserCreateRequest;
import com.example.usersapi.dto.UserResponse;
import com.example.usersapi.dto.UserUpdateRequest;
import com.example.usersapi.exception.DuplicateEmailException;
import com.example.usersapi.exception.UserNotFoundException;
import com.example.usersapi.model.User;
import com.example.usersapi.repository.UserRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(getUserOrThrow(id));
    }

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = getUserOrThrow(id);

        if (userRepository.existsByEmailAndIdNot(request.getEmail().trim(), id)) {
            throw new DuplicateEmailException(request.getEmail());
        }

        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim());
        user.setRole(request.getRole());

        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = getUserOrThrow(id);
        userRepository.delete(user);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        response.setPasswordHash(user.getPassword());
        return response;
    }
}
