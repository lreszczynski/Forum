package com.example.demo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component("Roles")
public class RoleContainer {
	public final String USER = "USER";
	public final String MODERATOR = "MODERATOR";
	public final String ADMIN = "ADMIN";
	
	public boolean isAtLeastModerator(UserDetails userDetails) {
		return CollectionUtils.containsAny(
				userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()),
				Arrays.asList(MODERATOR,ADMIN));
	}
}
