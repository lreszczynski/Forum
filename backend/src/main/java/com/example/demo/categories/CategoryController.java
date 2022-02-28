package com.example.demo.categories;

import com.example.demo.categories.dto.CategoryDTO;
import com.example.demo.categories.validation.CreateCategory;
import com.example.demo.categories.validation.UpdateCategory;
import com.example.demo.roles.dto.RoleDTO;
import com.example.demo.security.MyUserDetails;
import com.example.demo.security.SecurityUtility;
import com.example.demo.threads.dto.ThreadProjDTO;
import com.example.demo.threads.ThreadService;
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
	private final ThreadService threadService;
	
	@Autowired
	public CategoryController(CategoryService categoryService, ThreadService threadService) {
		this.categoryService = categoryService;
		this.threadService = threadService;
	}
	
	@Operation(summary = "Returns a list of categories")
	@GetMapping
	public ResponseEntity<Collection<CategoryDTO>> getAll() {
		List<CategoryDTO> categories = categoryService.findAll();
		return ResponseEntity.ok(categories);
	}
	
	@Operation(summary = "Returns a list of pinned threads by category id")
	@GetMapping("/{id}/pinned-threads")
	ResponseEntity<Collection<ThreadProjDTO>> getAllPinnedByCategoryId(@PathVariable Long id) {
		return ResponseEntity.ok(threadService.getAllPinnedByCategoryId(id));
	}
	
	@Operation(summary = "Returns a list of threads by category id")
	@PageableAsQueryParam
	@GetMapping("/{id}/threads")
	ResponseEntity<Page<ThreadProjDTO>> getAllByCategoryId(@PathVariable Long id, @Parameter(hidden = true) Pageable pageable) {
		long start = System.currentTimeMillis();
		Page<ThreadProjDTO> allByCategoryId = threadService.getAllByCategoryId(id, pageable);
		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		System.out.println(">>>>> "+timeElapsed);
		return ResponseEntity.ok(allByCategoryId);
	}
	
	@Operation(summary = "Get a category by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content)})
	@GetMapping("/{id}")
	ResponseEntity<CategoryDTO> getById(@PathVariable long id) {
		Optional<CategoryDTO> categoryDTO = categoryService.findById(id);
		if (categoryDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(categoryDTO.get());
	}
	
	@Operation(summary = "Create a new category")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_CREATED),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
	@PostMapping(consumes = APPLICATION_JSON_VALUE)
	@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	ResponseEntity<CategoryDTO> create(
			@Validated({Default.class, CreateCategory.class}) @RequestBody CategoryDTO categoryDTO) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = ((MyUserDetails) authentication.getPrincipal()).getUsername();
		CategoryDTO createdCategory = categoryService.create(categoryDTO);
		log.info(CATEGORY_CREATED_LOG, createdCategory);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
	}
	
	@Operation(summary = "Update a category by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
	@PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
	@PreAuthorize("@roleContainer.isAdmin(principal) || " +
			"(@roleContainer.isAtLeastModerator(principal) && @roleContainer.canEditCategory(principal, #id))")
	ResponseEntity<?> update(@Validated({Default.class, UpdateCategory.class}) @RequestBody CategoryDTO categoryDTO,
	                         @PathVariable Long id) {
		Optional<CategoryDTO> updatedCategory = categoryService.update(id, categoryDTO);
		if (updatedCategory.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(HTTP_NOT_FOUND_MESSAGE);
		}
		log.info(CATEGORY_UPDATED_LOG, updatedCategory);
		return ResponseEntity.ok(updatedCategory.get());
	}
	
	@Operation(summary = "Delete a category")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_NO_CONTENT),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
	@DeleteMapping("/{id}")
	@PreAuthorize("@roleContainer.isAdmin(principal) || " +
			"(@roleContainer.isAtLeastModerator(principal) && @roleContainer.canEditCategory(principal, #id))")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		categoryService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
	
	@Operation(summary = "Get roles of a category")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
	@GetMapping("/{id}/roles")
	@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	ResponseEntity<?> getRolesById(@PathVariable long id) {
		Optional<Set<RoleDTO>> rolesForCategoryById = categoryService.findRolesForCategoryById(id);
		if (rolesForCategoryById.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(rolesForCategoryById.get());
	}
	
	@Operation(summary = "Add role to category")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_CREATED),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
	@PostMapping(value = "/{id}/roles", consumes = APPLICATION_JSON_VALUE)
	@PreAuthorize("@roleContainer.isAdmin(principal) || " +
			"(@roleContainer.isAtLeastModerator(principal) && @roleContainer.canEditCategory(principal, #id))")
	ResponseEntity<?> addRoleToCategory(@RequestBody RoleDTO roleDTO, @PathVariable long id) {
		Optional<CategoryDTO> categoryDTO = categoryService.findById(id);
		if (categoryDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		categoryService.addRolesToCategory(categoryDTO.get(), roleDTO);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	@Operation(summary = "Delete role from category")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_NO_CONTENT),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
	@DeleteMapping(value = "/{id}/roles/{roleId}")
	@PreAuthorize("@roleContainer.isAdmin(principal) || " +
			"(@roleContainer.isAtLeastModerator(principal) && @roleContainer.canEditCategory(principal, #id))")
	ResponseEntity<?> deleteRoleFromCategory(@PathVariable long id, @PathVariable long roleId) {
		Optional<CategoryDTO> categoryDTO = categoryService.findById(id);
		categoryDTO.ifPresent(category -> categoryService.deleteRolesFromCategoryByIds(category, roleId));
		return ResponseEntity.noContent().build();
	}
}