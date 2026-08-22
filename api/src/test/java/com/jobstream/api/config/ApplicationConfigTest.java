package com.jobstream.api.config;

import com.jobstream.api.entity.Role;
import com.jobstream.api.entity.User;
import com.jobstream.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void userDetailsService_shouldReturnUser_whenEmailExists() {
        User user = new User();
        user.setEmail("alice@test.com");
        user.setPassword("encoded");
        user.setRole(Role.USER);
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));

        ApplicationConfig config = new ApplicationConfig(userRepository);
        UserDetailsService service = config.userDetailsService();

        UserDetails result = service.loadUserByUsername("alice@test.com");

        assertThat(result).isSameAs(user);
        assertThat(result.getUsername()).isEqualTo("alice@test.com");
    }

    @Test
    void userDetailsService_shouldThrowUsernameNotFound_whenNotExists() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        ApplicationConfig config = new ApplicationConfig(userRepository);
        UserDetailsService service = config.userDetailsService();

        assertThatThrownBy(() -> service.loadUserByUsername("unknown@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void passwordEncoder_shouldBeBCrypt() {
        ApplicationConfig config = new ApplicationConfig(userRepository);
        PasswordEncoder encoder = config.passwordEncoder();

        assertThat(encoder).isNotNull();
        String encoded = encoder.encode("password123");
        assertThat(encoder.matches("password123", encoded)).isTrue();
        assertThat(encoder.matches("wrong", encoded)).isFalse();
    }

    @Test
    void authenticationProvider_shouldNotBeNull() {
        ApplicationConfig config = new ApplicationConfig(userRepository);
        // passwordEncoder is used internally
        assertThat(config.authenticationProvider()).isNotNull();
    }
}
