package com.rolling.pokerly.user.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rolling.pokerly.user.domain.User;
import com.rolling.pokerly.user.dto.RegisterRequest;
import com.rolling.pokerly.user.dto.UserResponse;
import com.rolling.pokerly.user.exception.DuplicateNicknameException;
import com.rolling.pokerly.user.exception.UserNotFoundException;
import com.rolling.pokerly.user.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByNickname(req.getNickname())) {
            throw new DuplicateNicknameException();
        }
        User user = User.builder()
                .nickname(req.getNickname())
                .password(passwordEncoder.encode(req.getPassword()))
                .role("USER")
                .enabled(true)
                .build();
        userRepository.save(user);
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public User loadUser(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserNotFoundException());
    }

}
