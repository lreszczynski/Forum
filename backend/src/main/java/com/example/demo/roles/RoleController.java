package com.example.demo.roles;

import com.example.demo.roles.dto.RoleDTO;
import com.example.demo.roles.validation.CreateRole;
import com.example.demo.roles.validation.UpdateRole;
import com.example.demo.security.SecurityUtility;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
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
	@ApiResponse(responseCode = HTTP_OK)
	@PageableAsQueryParam
	@GetMapping
	ResponseEntity<Page<RoleDTO>> getAll(@Parameter(hidden = true) Pageable pageable) {
		Page<RoleDTO> categories = roleService.getAll(pageable);
		return ResponseEntity.ok(categories);
	}
	
	@Operation(summary = "Get a role by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content)})
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
			@ApiResponse(responseCode = HTTP_CREATED),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
	@PostMapping(consumes = APPLICATION_JSON_VALUE)
	@PreAuthorize("@roleContainer.isAdmin(principal)")
	ResponseEntity<RoleDTO> create(@Validated({Default.class, CreateRole.class}) @RequestBody RoleDTO roleDTO) {
		RoleDTO createdRole = roleService.create(roleDTO);
		log.info(ROLE_WAS_CREATED, createdRole);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
	}
	
	@Operation(summary = "Update a role by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
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
	
	/*@Operation(summary = "Delete a role")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_NO_CONTENT),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
	@DeleteMapping("/{id}")
	@PreAuthorize("@roleContainer.isAdmin(principal)")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		roleService.deleteById(id);
		return ResponseEntity.noContent().build();
	}*/
}