package com.example.demo.threads;

import com.example.demo.categories.CategoryService;
import com.example.demo.security.SecurityUtility;
import com.example.demo.threads.validation.CreateThread;
import com.example.demo.threads.validation.UpdateThread;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.example.demo.controller.HttpResponse.*;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(SecurityUtility.THREADS_PATH)
@Slf4j
public class ThreadController {
	private static final String THREAD_CREATED_LOG = "New thread was created: {}";
	private static final String THREAD_UPDATED_LOG = "Thread was updated: {}";
	
	private final ThreadService threadService;
	private final CategoryService categoryService;
	
	@Autowired
	public ThreadController(ThreadService threadService, CategoryService categoryService) {
		this.threadService = threadService;
		this.categoryService = categoryService;
	}
	
	@Operation(summary = "Returns a list of threads")
	@ApiResponse(responseCode = HTTP_OK, description = HTTP_OK_MESSAGE, content = {
			@Content(mediaType = APPLICATION_JSON_VALUE, array =
			@ArraySchema(schema = @Schema(implementation = ThreadDTO.class)))})
	@GetMapping
	ResponseEntity<Collection<ThreadDTO>> getAll() {
		List<ThreadDTO> threads = threadService.getAll();
		return ResponseEntity.ok(threads);
	}
	
	@Operation(summary = "Get a thread by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK, description = HTTP_OK_MESSAGE, content = {
					@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ThreadDTO.class))}),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, description = HTTP_NOT_FOUND_MESSAGE)})
	@GetMapping("/{id}")
	//@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	ResponseEntity<ThreadDTO> getById(@PathVariable long id) {
		Optional<ThreadDTO> threadDTO = threadService.getById(id);
		if (threadDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(threadDTO.get());
	}
	
	@Operation(summary = "Create a new thread")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_CREATED, description = HTTP_CREATED_MESSAGE, content = {
					@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ThreadDTO.class))}),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, description = HTTP_BAD_REQUEST_MESSAGE),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, description = HTTP_FORBIDDEN_MESSAGE),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = HTTP_UNAUTHORIZED_MESSAGE)})
	@PostMapping(consumes = APPLICATION_JSON_VALUE)
	//@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	ResponseEntity<ThreadDTO> create(@Validated({Default.class, CreateThread.class}) @RequestBody ThreadDTO threadDTO) {
		ThreadDTO createdThread = threadService.create(threadDTO);
		log.info(THREAD_CREATED_LOG, createdThread);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdThread);
	}
	
	@Operation(summary = "Update a thread by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK, description = HTTP_OK_MESSAGE, content = {
					@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ThreadDTO.class))}),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, description = HTTP_NOT_FOUND_MESSAGE, content = @Content),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, description = HTTP_BAD_REQUEST_MESSAGE),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, description = HTTP_FORBIDDEN_MESSAGE),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = HTTP_UNAUTHORIZED_MESSAGE)})
	@PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
	//@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	ResponseEntity<ThreadDTO> update(@Validated({Default.class, UpdateThread.class}) @RequestBody ThreadDTO threadDTO, @PathVariable Long id) {
		Optional<ThreadDTO> updatedThread = threadService.update(id, threadDTO);
		if (updatedThread.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		log.info(THREAD_UPDATED_LOG, updatedThread);
		return ResponseEntity.ok(updatedThread.get());
	}
	
	@Operation(summary = "Delete a thread")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_NO_CONTENT, description = HTTP_NO_CONTENT_MESSAGE),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, description = HTTP_FORBIDDEN_MESSAGE),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = HTTP_UNAUTHORIZED_MESSAGE)})
	@DeleteMapping("/{id}")
	//@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		threadService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}