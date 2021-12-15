package com.example.demo.role;

import com.example.demo.role.validation.CreateRole;
import com.example.demo.role.validation.UpdateRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("roles")
public
class RoleController {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(RoleController.class);
	
	private static final String ROLE_WAS_CREATED = "New role was created: {}";
	private static final String ROLE_WAS_UPDATED = "Role was updated: {}";
	
	private final RoleService roleService;
	
	@Autowired
	public RoleController(RoleService roleService) {
		this.roleService = roleService;
	}
	
	@Operation(summary = "Returns a list of roles sorted/filtered based on the query parameters")
	@ApiResponse(responseCode = "200", description = "List was returned", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Role.class))})
	@GetMapping("/")
	ResponseEntity<Collection<RoleDTO>> getAll() {
		List<RoleDTO> categories = roleService.getAll();
		return ResponseEntity.ok(categories);
	}
	
	@Operation(summary = "Get a role by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found the role", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Role.class))}),
			@ApiResponse(responseCode = "404", description = "Role not found", content = @Content)
	})
	@GetMapping("/{id}")
	ResponseEntity<RoleDTO> getById(@PathVariable long id) {
		Optional<RoleDTO> roleDTO = roleService.getById(id);
		if (roleDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(roleDTO.get());
	}
	
	@Operation(summary = "Create a new role")
	@ApiResponse(responseCode = "201", description = "Role was created", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Role.class))})
	@PostMapping(value = "/", consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<RoleDTO> create(@Validated({Default.class, CreateRole.class}) @RequestBody RoleDTO roleDTO) {
		RoleDTO createdRole = roleService.create(roleDTO);
		log.info(ROLE_WAS_CREATED, createdRole);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
	}
	
	@Operation(summary = "Update a role by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Role was updated", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Role.class))}),
			@ApiResponse(responseCode = "404", description = "Role not found", content = @Content)
	})
	@PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<RoleDTO> update(@Validated({Default.class, UpdateRole.class}) @RequestBody RoleDTO roleDTO, @PathVariable Long id) {
		Optional<RoleDTO> updatedRole = roleService.update(id, roleDTO);
		if (updatedRole.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		log.info(ROLE_WAS_UPDATED, updatedRole);
		return ResponseEntity.ok(updatedRole.get());
	}
	
	@Operation(summary = "Delete a role")
	@ApiResponse(responseCode = "204", description = "Role was deleted", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Role.class))})
	@DeleteMapping("/{id}")
	public ResponseEntity<Long> delete(@PathVariable Long id) {
		roleService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}