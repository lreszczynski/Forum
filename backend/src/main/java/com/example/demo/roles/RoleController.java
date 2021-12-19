package com.example.demo.roles;

import com.example.demo.categories.CategoryDTO;
import com.example.demo.roles.validation.CreateRole;
import com.example.demo.roles.validation.UpdateRole;
import com.example.demo.security.SecurityUtility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.example.demo.controller.HttpResponse.*;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(SecurityUtility.ROLES_PATH)
@Slf4j
public class RoleController {
	private static final String ROLE_WAS_CREATED = "New role was created: {}";
	private static final String ROLE_WAS_UPDATED = "Role was updated: {}";
	
	private final RoleService roleService;
	
	@Autowired
	public RoleController(RoleService roleService) {
		this.roleService = roleService;
	}
	
	@Operation(summary = "Returns a list of roles")
	@ApiResponse(responseCode = HTTP_OK, description = HTTP_OK_MESSAGE, content = {
			@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = RoleDTO.class))})
	@GetMapping
	ResponseEntity<Collection<RoleDTO>> getAll() {
		List<RoleDTO> categories = roleService.getAll();
		return ResponseEntity.ok(categories);
	}
	
	@Operation(summary = "Get a role by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK, description = HTTP_OK_MESSAGE, content = {
					@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = RoleDTO.class))}),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, description = HTTP_NOT_FOUND_MESSAGE)})
	@GetMapping("/{id}")
	ResponseEntity<RoleDTO> getById(@PathVariable long id) {
		Optional<RoleDTO> roleDTO = roleService.getById(id);
		if (roleDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(roleDTO.get());
	}
	
	@Operation(summary = "Create a new role")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_CREATED, description = HTTP_CREATED_MESSAGE, content = {
					@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoryDTO.class))}),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, description = HTTP_BAD_REQUEST_MESSAGE),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, description = HTTP_FORBIDDEN_MESSAGE),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = HTTP_UNAUTHORIZED_MESSAGE)})
	@PostMapping(consumes = APPLICATION_JSON_VALUE)
	@PreAuthorize("@roleContainer.isAdmin(principal)")
	ResponseEntity<RoleDTO> create(@Validated({Default.class, CreateRole.class}) @RequestBody RoleDTO roleDTO) {
		RoleDTO createdRole = roleService.create(roleDTO);
		log.info(ROLE_WAS_CREATED, createdRole);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
	}
	
	@Operation(summary = "Update a role by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK, description = HTTP_OK_MESSAGE, content = {
					@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoryDTO.class))}),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, description = HTTP_NOT_FOUND_MESSAGE, content = @Content),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, description = HTTP_BAD_REQUEST_MESSAGE),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, description = HTTP_FORBIDDEN_MESSAGE),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = HTTP_UNAUTHORIZED_MESSAGE)})
	@PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
	@PreAuthorize("@roleContainer.isAdmin(principal)")
	ResponseEntity<RoleDTO> update(@Validated({Default.class, UpdateRole.class}) @RequestBody RoleDTO roleDTO, @PathVariable Long id) {
		Optional<RoleDTO> updatedRole = roleService.update(id, roleDTO);
		if (updatedRole.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		log.info(ROLE_WAS_UPDATED, updatedRole);
		return ResponseEntity.ok(updatedRole.get());
	}
	
	@Operation(summary = "Delete a role")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_NO_CONTENT, description = HTTP_NO_CONTENT_MESSAGE),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, description = HTTP_FORBIDDEN_MESSAGE),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = HTTP_UNAUTHORIZED_MESSAGE)})
	@DeleteMapping("/{id}")
	@PreAuthorize("@roleContainer.isAdmin(principal)")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		roleService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}