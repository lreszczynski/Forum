package com.example.demo;

import com.example.demo.security.RoleContainer;
import org.springframework.beans.factory.annotation.Autowired;
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
public class TestUserDetailsService {
	@Autowired
	RoleContainer roleContainer;
	
	@Bean
	@Primary
	public UserDetailsService userDetailsService() {
		User user = new User("User", "",
				List.of(
						new SimpleGrantedAuthority(roleContainer.USER)
				));
		
		User moderator = new User("Moderator", "",
				List.of(
						new SimpleGrantedAuthority(roleContainer.MODERATOR)
				));
		
		User admin = new User("Admin", "",
				List.of(
						new SimpleGrantedAuthority(roleContainer.ADMIN)
				));
		
		
		return new InMemoryUserDetailsManager(Arrays.asList(
				user, moderator, admin
		));
	}
}
