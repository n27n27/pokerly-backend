package com.rolling.pokerly.security.jwt;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomPrincipal implements UserDetails {

    private final Long userId;
    private final String nickname;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomPrincipal(Long userId, String nickname, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.nickname = nickname;
        this.authorities = authorities;
    }

    public Long getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override public String getPassword() { return ""; }
    @Override public String getUsername() { return nickname; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
