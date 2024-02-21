package com.planbow.security.services;

import com.planbow.documents.users.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Data
public class UserDetailsImpl implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;

    private final String username;

    private final String email;
    @JsonIgnore
    private final String password;

    private List<GrantedAuthority> authorities;

    public UserDetailsImpl(String id, String username, String email, String password,
                           List<GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getEmail(),
                user.getPassword(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
