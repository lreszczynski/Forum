package com.example.demo.security;

import com.example.demo.categories.CategoryService;
import com.example.demo.roles.RoleDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleContainer {
	public static final String USER = "USER";
	public static final String MODERATOR = "MODERATOR";
	public static final String ADMIN = "ADMIN";
	
	private CategoryService categoryService;
	
	public RoleContainer(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
	
	public boolean isAdmin(UserDetails userDetails) {
		return userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()).contains(ADMIN);
	}
	
	public boolean isAtLeastModerator(UserDetails userDetails) {
		return CollectionUtils.containsAny(
				userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()),
				Arrays.asList(MODERATOR,ADMIN));
	}
	
	public boolean canEditCategory(UserDetails userDetails, Long id) {
		List<String> authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
		List<String> allowedRoles = categoryService.getRolesForCategoryById(id).stream().map(RoleDTO::getName).map(String::toUpperCase).collect(Collectors.toList());
		
		return CollectionUtils.containsAny(allowedRoles, authorities);
	}
}
