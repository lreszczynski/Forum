package com.example.demo.users;

import com.example.demo.security.MyUserDetails;
import com.example.demo.security.SecurityUtility;
import com.example.demo.users.dto.UserDTO;
import com.example.demo.users.dto.UserProfileDTO;
import com.example.demo.users.dto.UserRegistrationDTO;
import com.example.demo.users.validation.UpdateUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
import java.util.Optional;

import static com.example.demo.controller.HttpResponse.*;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(SecurityUtility.USERS_PATH)
@Slf4j
public class UserController {
	private static final String USER_CREATED_LOG = "New user was created: {}";
	private static final String USER_UPDATED_LOG = "User was updated: {}";
	
	private final UserService userService;
	
	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	@Operation(summary = "Returns a list of users")
	@ApiResponse(responseCode = HTTP_OK)
	@PageableAsQueryParam
	@PreAuthorize("@roleContainer.isAdmin(principal)")
	@GetMapping
	ResponseEntity<Page<UserDTO>> getAll(@Parameter(hidden = true) Pageable pageable) {
		Page<UserDTO> users = userService.getAll(pageable);
		return ResponseEntity.ok(users);
	}
	
	@Operation(summary = "Get user account settings")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content)})
	@GetMapping("/account")
	ResponseEntity<UserDTO> getById() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Long id = ((MyUserDetails) authentication.getPrincipal()).getId();
		Optional<UserDTO> userDTO = userService.getUserAccount(id);
		if (userDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(userDTO.get());
	}
	
	@Operation(summary = "Get user's public profile")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content)})
	@GetMapping("/profile/{id}")
	ResponseEntity<UserProfileDTO> getProfile(@PathVariable long id) {
		Optional<UserProfileDTO> optionalUserBasicDTO = userService.getUserPublicProfile(id);
		if (optionalUserBasicDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(optionalUserBasicDTO.get());
	}
	
	@Operation(summary = "Update a user by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
	@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	@PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<UserDTO> update(@Validated({Default.class, UpdateUser.class}) @RequestBody UserDTO userDTO,
	                               @PathVariable Long id) {
		Optional<UserDTO> updatedUser = userService.update(userDTO);
		if (updatedUser.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		log.info(USER_UPDATED_LOG, updatedUser);
		return ResponseEntity.ok(updatedUser.get());
	}
	
	@Operation(summary = "Delete a user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_NO_CONTENT, content = @Content),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
	@PreAuthorize("@roleContainer.isAdmin(principal)")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		userService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
	
	@Operation(summary = "Register a new user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, content = @Content)})
	@PostMapping(value = "/register", consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<UserDTO> register(@Validated @RequestBody UserRegistrationDTO userRegistrationDTO) {
		UserDTO createdUser = userService.register(userRegistrationDTO);
		log.info(USER_CREATED_LOG, createdUser);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
	}
}