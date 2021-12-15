package com.example.demo.threads;

import com.example.demo.security.SecurityUtility;
import com.example.demo.threads.validation.CreateThread;
import com.example.demo.threads.validation.UpdateThread;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(SecurityUtility.THREADS_PATH)
@Slf4j
public class ThreadController {
	// TODO @ApiResponse @Schema DTO
	private static final String THREAD_CREATED_LOG = "New thread was created: {}";
	private static final String THREAD_UPDATED_LOG = "Thread was updated: {}";
	
	private final ThreadService threadService;
	
	@Autowired
	public ThreadController(ThreadService threadService) {
		this.threadService = threadService;
	}
	
	@Operation(summary = "Returns a list of threads")
	@ApiResponse(responseCode = "200", description = "List was returned", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Thread.class))})
	@GetMapping("/")
	ResponseEntity<Collection<ThreadDTO>> getAll() {
		List<ThreadDTO> threads = threadService.getAll();
		return ResponseEntity.ok(threads);
	}
	
	@Operation(summary = "Get a thread by its id")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the thread", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Thread.class))}),
			@ApiResponse(responseCode = "404", description = "Thread not found", content = @Content)})
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
	@ApiResponse(responseCode = "201", description = "Thread is created", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Thread.class))})
	@PostMapping(value = "/", consumes = APPLICATION_JSON_VALUE)
	//@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	ResponseEntity<ThreadDTO> create(@Validated({Default.class, CreateThread.class}) @RequestBody ThreadDTO threadDTO) {
		ThreadDTO createdThread = threadService.create(threadDTO);
		log.info(THREAD_CREATED_LOG, createdThread);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdThread);
	}
	
	@Operation(summary = "Update a thread by its id")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Thread was updated", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Thread.class))}),
			@ApiResponse(responseCode = "404", description = "Thread not found", content = @Content)})
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
	@ApiResponse(responseCode = "204", description = "Thread was deleted", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Thread.class))})
	@DeleteMapping("/{id}")
	//@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		threadService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}