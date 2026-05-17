package com.example.usersapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.usersapi.dto.UserCreateRequest;
import com.example.usersapi.dto.UserResponse;
import com.example.usersapi.dto.UserUpdateRequest;
import com.example.usersapi.exception.DuplicateEmailException;
import com.example.usersapi.exception.UserNotFoundException;
import com.example.usersapi.model.Role;
import com.example.usersapi.model.User;
import com.example.usersapi.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createUser_hashesPasswordAndMapsPasswordHash() {
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPassword("securepass123");
        request.setRole(Role.USER);

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("securepass123")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(Instant.parse("2026-05-16T12:00:00Z"));
            return user;
        });

        UserResponse response = userService.createUser(request);

        assertThat(response.getPasswordHash()).isEqualTo("$2a$10$hashed");
        assertThat(response.getEmail()).isEqualTo("jane@example.com");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("$2a$10$hashed");
    }

    @Test
    void createUser_throwsWhenEmailExists() {
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Jane");
        request.setEmail("jane@example.com");
        request.setPassword("securepass123");
        request.setRole(Role.USER);

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateEmailException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void findById_returnsUser() {
        User user = sampleUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPasswordHash()).isEqualTo("$2a$10$stored");
    }

    @Test
    void findById_throwsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findAll_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser(1L), sampleUser(2L)));

        List<UserResponse> responses = userService.findAll();

        assertThat(responses).hasSize(2);
    }

    @Test
    void updateUser_reHashesWhenPasswordProvided() {
        User existing = sampleUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("jane@example.com", 1L)).thenReturn(false);
        when(passwordEncoder.encode("newpassword1")).thenReturn("$2a$10$newhash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Jane Updated");
        request.setEmail("jane@example.com");
        request.setPassword("newpassword1");
        request.setRole(Role.ADMIN);

        UserResponse response = userService.updateUser(1L, request);

        assertThat(response.getPasswordHash()).isEqualTo("$2a$10$newhash");
        assertThat(existing.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void updateUser_keepsPasswordWhenOmitted() {
        User existing = sampleUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("jane@example.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Jane Updated");
        request.setEmail("jane@example.com");
        request.setRole(Role.USER);

        UserResponse response = userService.updateUser(1L, request);

        assertThat(response.getPasswordHash()).isEqualTo("$2a$10$stored");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deleteUser_removesUser() {
        User existing = sampleUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        userService.deleteUser(1L);

        verify(userRepository).delete(existing);
    }

    private User sampleUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setPassword("$2a$10$stored");
        user.setRole(Role.USER);
        user.setCreatedAt(Instant.parse("2026-05-16T12:00:00Z"));
        return user;
    }
}
