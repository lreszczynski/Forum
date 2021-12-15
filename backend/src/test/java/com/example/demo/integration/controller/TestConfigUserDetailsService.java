package com.example.demo.integration.controller;

import com.example.demo.security.RoleContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Arrays;
import java.util.List;

@TestConfiguration
public class TestConfigUserDetailsService {
	@Bean
	@Primary
	public UserDetailsService userDetailsService() {
		User user = new User("User", "",
				List.of(
						new SimpleGrantedAuthority(RoleContainer.USER)
				));
		
		User moderator = new User("Moderator", "",
				List.of(
						new SimpleGrantedAuthority(RoleContainer.MODERATOR)
				));
		
		User admin = new User("Admin", "",
				List.of(
						new SimpleGrantedAuthority(RoleContainer.ADMIN)
				));
		
		
		return new InMemoryUserDetailsManager(Arrays.asList(
				user, moderator, admin
		));
	}
}
