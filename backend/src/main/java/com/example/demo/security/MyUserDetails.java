package com.example.demo.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class MyUserDetails implements UserDetails {
	private final Long id;
	private final String username;
	private final String password;
	private final Collection<GrantedAuthority> authorities;
	private final boolean active;
	private final boolean banned;
	
	public MyUserDetails(Long id, String username, String password, Collection<GrantedAuthority> authorities, boolean active, boolean banned) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.authorities = authorities;
		this.active = active;
		this.banned = banned;
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
}
