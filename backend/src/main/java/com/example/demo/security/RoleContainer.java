package com.example.demo.security;

import com.example.demo.categories.CategoryDTO;
import com.example.demo.categories.CategoryService;
import com.example.demo.posts.PostDTO;
import com.example.demo.posts.PostService;
import com.example.demo.roles.RoleDTO;
import com.example.demo.threads.ThreadDTO;
import com.example.demo.threads.ThreadService;
import com.example.demo.users.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleContainer {
	public static final String USER = "USER";
	public static final String MODERATOR = "MODERATOR";
	public static final String ADMIN = "ADMIN";
	
	private CategoryService categoryService;
	private ThreadService threadService;
	private UserService userService;
	private PostService postService;
	
	public RoleContainer(CategoryService categoryService, ThreadService threadService,
	                     UserService userService, PostService postService) {
		this.categoryService = categoryService;
		this.threadService = threadService;
		this.userService = userService;
		this.postService = postService;
	}
	
	public boolean isAdmin(MyUserDetails userDetails) {
		return userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()).contains(ADMIN);
	}
	
	public boolean isAtLeastModerator(MyUserDetails userDetails) {
		return CollectionUtils.containsAny(
				userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()),
				Arrays.asList(MODERATOR,ADMIN));
	}
	
	public boolean canEditCategory(MyUserDetails userDetails, Long categoryId) {
		if (isAdmin(userDetails))
			return true;
		if (!isAtLeastModerator(userDetails))
			return false;
		
		List<String> authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
		Optional<Set<RoleDTO>> optional = categoryService.findRolesForCategoryById(categoryId);
		if (optional.isPresent()) {
			List<String> allowedRoles = optional.get().stream().map(roleDTO -> roleDTO.getName().toUpperCase()).collect(Collectors.toList());
			return CollectionUtils.containsAny(allowedRoles, authorities);
		}
		return false;
	}
	
	public boolean canCreateThread(MyUserDetails userDetails, Long categoryId) {
		if (isAdmin(userDetails))
			return true;
		Optional<CategoryDTO> optionalCategoryDTO = categoryService.findById(categoryId);
		if (optionalCategoryDTO.isEmpty() || !optionalCategoryDTO.get().isActive())
			return false;
		
		List<String> authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
		Optional<Set<RoleDTO>> optional = categoryService.findRolesForCategoryById(categoryId);
		if (optional.isPresent()) {
			List<String> allowedRoles = optional.get().stream().map(roleDTO -> roleDTO.getName().toUpperCase()).collect(Collectors.toList());
			return CollectionUtils.containsAny(allowedRoles, authorities);
		}
		return false;
	}
	
	public boolean canEditThread(MyUserDetails userDetails, Long threadId) {
		if (isAdmin(userDetails))
			return true;
		if (!isAtLeastModerator(userDetails))
			return false;
		
		Optional<ThreadDTO> optionalThreadDTO = threadService.findById(threadId);
		if (optionalThreadDTO.isPresent()) {
			List<String> authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
			Optional<Set<RoleDTO>> optional = categoryService.findRolesForCategoryById(optionalThreadDTO.get().getCategory().getId());
			if (optional.isPresent()) {
				List<String> allowedRoles = optional.get().stream().map(roleDTO -> roleDTO.getName().toUpperCase()).collect(Collectors.toList());
				return CollectionUtils.containsAny(allowedRoles, authorities);
			}
		}
		
		return false;
	}
	
	public boolean canCreatePost(MyUserDetails userDetails, Long threadId) {
		if (isAdmin(userDetails))
			return true;
		
		Optional<ThreadDTO> optionalThreadDTO = threadService.findById(threadId);
		
		if (optionalThreadDTO.isPresent()) {
			if (!optionalThreadDTO.get().isActive())
				return false;
			
			CategoryDTO category = optionalThreadDTO.get().getCategory();
			List<String> authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
			Optional<Set<RoleDTO>> optional = categoryService.findRolesForCategoryById(category.getId());
			if (optional.isPresent()) {
				List<String> allowedRoles = optional.get().stream().map(roleDTO -> roleDTO.getName().toUpperCase()).collect(Collectors.toList());
				return CollectionUtils.containsAny(allowedRoles, authorities);
			}
		}
		
		return false;
	}
	
	public boolean canEditPost(MyUserDetails userDetails, Long postId) {
		if (isAdmin(userDetails))
			return true;
		
		Optional<PostDTO> optionalPostDTO = postService.getById(postId);
		
		if (optionalPostDTO.isPresent()) {
			if (userDetails.getId().equals(optionalPostDTO.get().getUser().getId()))
				return true;
			
			ThreadDTO threadDTO = optionalPostDTO.get().getThread();
			CategoryDTO category = threadDTO.getCategory();
			List<String> authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
			Optional<Set<RoleDTO>> optional = categoryService.findRolesForCategoryById(category.getId());
			if (optional.isPresent()) {
				List<String> allowedRoles = optional.get().stream().map(roleDTO -> roleDTO.getName().toUpperCase()).collect(Collectors.toList());
				return CollectionUtils.containsAny(allowedRoles, authorities);
			}
		}
		
		return false;
	}
	
	public boolean isNotBanned(MyUserDetails userDetails) {
		return !userDetails.isBanned();
	}
	
	public boolean doesIdMatch(MyUserDetails userDetails, Long id) {
		return userDetails.getId().equals(id);
	}
}
