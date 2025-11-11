package com.rolling.pokerly.security;

import com.rolling.pokerly.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {
        var u = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UsernameNotFoundException("Not found: " + nickname));

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole()));
        return new org.springframework.security.core.userdetails.User(
                u.getNickname(), u.getPassword(), u.isEnabled(),
                true, true, true, authorities
        );
    }
}
