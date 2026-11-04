package com.velas.transitAds.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.velas.transitAds.core.domain.AccountType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record AuthenticatedUser(String id, String email, AccountType accountType, boolean verified,
                                List<String> roles) implements UserDetails {

    @JsonCreator
    public AuthenticatedUser(
            @JsonProperty("id") String id,
            @JsonProperty("email") String email,
            @JsonProperty("accountType") AccountType accountType,
            @JsonProperty("verified") boolean verified,
            @JsonProperty("roles") List<String> roles
    ) {
        this.id = id;
        this.email = email;
        this.accountType = accountType;
        this.verified = verified;
        this.roles = roles;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return this.email;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return null;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}