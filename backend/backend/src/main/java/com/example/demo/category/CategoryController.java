package com.example.demo.category;

import com.example.demo.category.validation.CreateCategory;
import com.example.demo.category.validation.UpdateCategory;
import com.example.demo.security.SecurityUtility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
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

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(SecurityUtility.CATEGORIES_PATH)
public
class CategoryController {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(CategoryController.class);
	
	private static final String CATEGORY_CREATED_LOG = "New category was created: {}";
	private static final String CATEGORY_UPDATED_LOG = "Category was updated: {}";
	
	private final CategoryService categoryService;
	
	@Autowired
	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
	
	@Operation(summary = "Returns a list of categories")
	@ApiResponse(responseCode = "200", description = "List was returned", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Category.class))})
	@GetMapping("/")
	ResponseEntity<Collection<CategoryDTO>> getAll() {
		List<CategoryDTO> categories = categoryService.getAll();
		return ResponseEntity.ok(categories);
	}
	
	@Operation(summary = "Get a category by its id")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the category", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Category.class))}),
			@ApiResponse(responseCode = "404", description = "Category not found", content = @Content)})
	@GetMapping("/{id}")
	@PreAuthorize("@Roles.isAtLeastModerator(principal)")
	ResponseEntity<CategoryDTO> getById(@PathVariable long id) {
		Optional<CategoryDTO> categoryDTO = categoryService.getById(id);
		if (categoryDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(categoryDTO.get());
	}
	
	@Operation(summary = "Create a new category")
	@ApiResponse(responseCode = "201", description = "Category is created", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Category.class))})
	@PostMapping(value = "/", consumes = APPLICATION_JSON_VALUE)
	@PreAuthorize("@Roles.isAtLeastModerator(principal)")
	ResponseEntity<CategoryDTO> create(@Validated({Default.class, CreateCategory.class}) @RequestBody CategoryDTO categoryDTO) {
		CategoryDTO createdCategory = categoryService.create(categoryDTO);
		log.info(CATEGORY_CREATED_LOG, createdCategory);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
	}
	
	@Operation(summary = "Update a category by its id")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Category was updated", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Category.class))}),
			@ApiResponse(responseCode = "404", description = "Category not found", content = @Content)})
	@PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
	@PreAuthorize("@Roles.isAtLeastModerator(principal)")
	ResponseEntity<CategoryDTO> update(@Validated({Default.class, UpdateCategory.class}) @RequestBody CategoryDTO categoryDTO, @PathVariable Long id) {
		Optional<CategoryDTO> updatedCategory = categoryService.update(id, categoryDTO);
		if (updatedCategory.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		log.info(CATEGORY_UPDATED_LOG, updatedCategory);
		return ResponseEntity.ok(updatedCategory.get());
	}
	
	@Operation(summary = "Delete a category")
	@ApiResponse(responseCode = "204", description = "Category was deleted", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Category.class))})
	@DeleteMapping("/{id}")
	@PreAuthorize("@Roles.isAtLeastModerator(principal)")
	public ResponseEntity<Long> delete(@PathVariable Long id) {
		categoryService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}