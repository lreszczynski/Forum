package com.example.demo.categories;

import com.example.demo.categories.validation.CreateCategory;
import com.example.demo.categories.validation.UpdateCategory;
import com.example.demo.roles.RoleDTO;
import com.example.demo.security.MyUserDetails;
import com.example.demo.security.SecurityUtility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.example.demo.controller.HttpResponse.*;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(SecurityUtility.CATEGORIES_PATH)
@Slf4j
public class CategoryController {
	private static final String CATEGORY_CREATED_LOG = "New category was created: {}";
	private static final String CATEGORY_UPDATED_LOG = "Category was updated: {}";
	
	private final CategoryService categoryService;
	
	@Autowired
	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
	
	@Operation(summary = "Returns a list of categories")
	@ApiResponse(responseCode = HTTP_OK, description = HTTP_OK_MESSAGE, content = {
			@Content(mediaType = APPLICATION_JSON_VALUE, array =
			@ArraySchema(schema = @Schema(implementation = CategoryDTO.class)))})
	@GetMapping
	ResponseEntity<Collection<CategoryDTO>> getAll() {
		List<CategoryDTO> categories = categoryService.getAll();
		return ResponseEntity.ok(categories);
	}
	
	@Operation(summary = "Get a category by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK, description = HTTP_OK_MESSAGE, content = {
					@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoryDTO.class))}),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, description = HTTP_NOT_FOUND_MESSAGE)})
	@GetMapping("/{id}")
	ResponseEntity<CategoryDTO> getById(@PathVariable long id) {
		Optional<CategoryDTO> categoryDTO = categoryService.getById(id);
		if (categoryDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(categoryDTO.get());
	}
	
	@Operation(summary = "Create a new category")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_CREATED, description = HTTP_CREATED_MESSAGE, content = {
					@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoryDTO.class))}),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, description = HTTP_BAD_REQUEST_MESSAGE),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, description = HTTP_FORBIDDEN_MESSAGE),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = HTTP_UNAUTHORIZED_MESSAGE)})
	@PostMapping(consumes = APPLICATION_JSON_VALUE)
	@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	ResponseEntity<CategoryDTO> create(@Validated({Default.class, CreateCategory.class}) @RequestBody CategoryDTO categoryDTO) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = ((MyUserDetails) authentication.getPrincipal()).getUsername();
		CategoryDTO createdCategory = categoryService.create(categoryDTO, username);
		log.info(CATEGORY_CREATED_LOG, createdCategory);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
	}
	
	@Operation(summary = "Update a category by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK, description = HTTP_OK_MESSAGE, content = {
					@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoryDTO.class))}),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, description = HTTP_NOT_FOUND_MESSAGE, content = @Content),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, description = HTTP_BAD_REQUEST_MESSAGE),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, description = HTTP_FORBIDDEN_MESSAGE),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = HTTP_UNAUTHORIZED_MESSAGE)})
	@PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
	@PreAuthorize("@roleContainer.isAdmin(principal) || " +
			"(@roleContainer.isAtLeastModerator(principal) && @roleContainer.canEditCategory(principal, #id))")
	ResponseEntity<?> update(@Validated({Default.class, UpdateCategory.class}) @RequestBody CategoryDTO categoryDTO, @PathVariable Long id) {
		if (!id.equals(categoryDTO.getId())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HTTP_BAD_REQUEST_MESSAGE);
		}
		Optional<CategoryDTO> updatedCategory = categoryService.update(id, categoryDTO);
		if (updatedCategory.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(HTTP_NOT_FOUND_MESSAGE);
		}
		log.info(CATEGORY_UPDATED_LOG, updatedCategory);
		return ResponseEntity.ok(updatedCategory.get());
	}
	
	@Operation(summary = "Delete a category")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_NO_CONTENT, description = HTTP_NO_CONTENT_MESSAGE),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, description = HTTP_FORBIDDEN_MESSAGE),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = HTTP_UNAUTHORIZED_MESSAGE)})
	@DeleteMapping("/{id}")
	@PreAuthorize("@roleContainer.isAdmin(principal) || " +
			"(@roleContainer.isAtLeastModerator(principal) && @roleContainer.canEditCategory(principal, #id))")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		categoryService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
	
	@Operation(summary = "Get roles of a category")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK, description = HTTP_OK_MESSAGE, content = {
					@Content(mediaType = APPLICATION_JSON_VALUE, array =
					@ArraySchema(schema = @Schema(implementation = RoleDTO.class)))}),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, description = HTTP_NOT_FOUND_MESSAGE)})
	@GetMapping("/{id}/roles")
	@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	ResponseEntity<?> getRolesById(@PathVariable long id) {
		Optional<CategoryDTO> categoryDTO = categoryService.getById(id);
		if (categoryDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Set<RoleDTO> rolesForCategoryById = categoryService.getRolesForCategoryById(id);
		return ResponseEntity.ok(rolesForCategoryById);
	}
	
	@Operation(summary = "Add role to category")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_CREATED, description = HTTP_CREATED_MESSAGE, content = {
					@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoryDTO.class))}),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, description = HTTP_BAD_REQUEST_MESSAGE),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, description = HTTP_FORBIDDEN_MESSAGE),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = HTTP_UNAUTHORIZED_MESSAGE),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, description = HTTP_NOT_FOUND_MESSAGE)})
	@PostMapping(value = "/{id}/roles", consumes = APPLICATION_JSON_VALUE)
	@PreAuthorize("@roleContainer.isAdmin(principal) || " +
			"(@roleContainer.isAtLeastModerator(principal) && @roleContainer.canEditCategory(principal, #id))")
	ResponseEntity<?> addRoleToCategory(@RequestBody RoleDTO roleDTO, @PathVariable long id) {
		Optional<CategoryDTO> categoryDTO = categoryService.getById(id);
		if (categoryDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		categoryService.addRolesToCategory(categoryDTO.get(), roleDTO);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	@Operation(summary = "Delete role from category")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_NO_CONTENT, description = HTTP_NO_CONTENT_MESSAGE),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, description = HTTP_BAD_REQUEST_MESSAGE),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, description = HTTP_FORBIDDEN_MESSAGE),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = HTTP_UNAUTHORIZED_MESSAGE)})
	@DeleteMapping(value = "/{id}/roles/{roleId}")
	@PreAuthorize("@roleContainer.isAdmin(principal) || " +
			"(@roleContainer.isAtLeastModerator(principal) && @roleContainer.canEditCategory(principal, #id))")
	ResponseEntity<?> deleteRoleFromCategory(@RequestBody RoleDTO roleDTO, @PathVariable long id, @PathVariable long roleId) {
		if (roleId != roleDTO.getId()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HTTP_BAD_REQUEST_MESSAGE);
		}
		Optional<CategoryDTO> categoryDTO = categoryService.getById(id);
		categoryDTO.ifPresent(category -> categoryService.deleteRolesFromCategoryByIds(category, roleId));
		return ResponseEntity.noContent().build();
	}
}