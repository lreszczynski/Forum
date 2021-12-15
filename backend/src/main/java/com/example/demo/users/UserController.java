package com.example.demo.users;

import com.example.demo.users.validation.UpdateUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
import java.util.*;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("users")
@Slf4j
public class UserController {
	private static final String USER_CREATED_LOG = "New user was created: {}";
	private static final String USER_UPDATED_LOG = "User was updated: {}";
	
	private final UserService userService;
	
	@Autowired
	public UserController(UserService userService, ObjectMapper objectMapper) {
		this.userService = userService;
	}
	
	@Operation(summary = "Returns a list of users sorted/filtered based on the query parameters")
	@ApiResponse(responseCode = "200", description = "List was returned", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = User.class))})
	@GetMapping("/")
	ResponseEntity<Collection<UserDTO>> getAll() {
		List<UserDTO> users = userService.getAll();
		return ResponseEntity.ok(users);
	}
	
	@Operation(summary = "Get a user by its id")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the user", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = User.class))}),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content)})
	@GetMapping("/{id}")
	ResponseEntity<UserDTO> getById(@PathVariable long id) {
		Optional<UserDTO> userDTO = userService.getById(id);
		if (userDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(userDTO.get());
	}
	
	@Operation(summary = "Update a user by its id")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User was updated", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = User.class))}),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content)})
	@PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<UserDTO> update(@Validated({Default.class, UpdateUser.class}) @RequestBody UserDTO userDTO, @PathVariable Long id) {
		Optional<UserDTO> updatedUser = userService.update(id, userDTO);
		if (updatedUser.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		log.info(USER_UPDATED_LOG, updatedUser);
		return ResponseEntity.ok(updatedUser.get());
	}
	
	@Operation(summary = "Delete a user")
	@ApiResponse(responseCode = "204", description = "User was deleted", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = User.class))})
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		userService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
	
	@Operation(summary = "Register a new user")
	@ApiResponse(responseCode = "201", description = "User was registered", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = User.class))})
	@PostMapping(value = "/register", consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<UserDTO> register(@Validated @RequestBody UserRegistrationDTO userRegistrationDTO) {
		UserDTO createdUser = userService.register(userRegistrationDTO);
		log.info(USER_CREATED_LOG, createdUser);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
	}
	
	/*@Operation(summary = "Login")
	@ApiResponse(responseCode = "201", description = "User was authenticated", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = User.class))})
	@PostMapping(value = "/", consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<UserDTO> authenticate(@Validated @RequestBody UserLoginDTO userLoginDTO) {
		UserDTO createdUser = userService.register(userDTO);
		log.info(USER_CREATED_LOG, createdUser);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
	}*/
}