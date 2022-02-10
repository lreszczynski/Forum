package com.example.demo.threads;

import com.example.demo.categories.CategoryService;
import com.example.demo.posts.PostDTO;
import com.example.demo.posts.PostService;
import com.example.demo.security.MyUserDetails;
import com.example.demo.security.SecurityUtility;
import com.example.demo.threads.validation.UpdateThread;
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
	private final PostService postService;
	private final CategoryService categoryService;
	
	@Autowired
	public ThreadController(ThreadService threadService, PostService postService, CategoryService categoryService) {
		this.threadService = threadService;
		this.postService = postService;
		this.categoryService = categoryService;
	}
	
	@Operation(summary = "Returns a list of threads")
	@ApiResponse(responseCode = HTTP_OK)
	@GetMapping
	ResponseEntity<Collection<ThreadWithPostsCountDTO>> getAll() {
		return ResponseEntity.ok(threadService.getAll());
	}
	
	@Operation(summary = "Get a thread by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content)})
	@GetMapping("/{id}")
		//@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	ResponseEntity<ThreadDTO> getById(@PathVariable long id) {
		Optional<ThreadDTO> threadDTO = threadService.findById(id);
		if (threadDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(threadDTO.get());
	}
	
	@Operation(summary = "Get posts by thread id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content)})
	@PageableAsQueryParam
	@GetMapping("/{id}/posts")
		//@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	ResponseEntity<Page<PostDTO>> getByIdWithPosts(@PathVariable long id, @Parameter(hidden = true) Pageable pageable) {
		Page<PostDTO> postDTOPage = postService.getAllByThreadId(id, pageable);
		return ResponseEntity.ok(postDTOPage);
	}
	
	@Operation(summary = "Create a new thread")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_CREATED),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
	@PostMapping(consumes = APPLICATION_JSON_VALUE)
	@PreAuthorize("@roleContainer.canCreateThread(principal, #threadDTO.categoryId)")
	ResponseEntity<ThreadDTO> create(@Validated @RequestBody ThreadCreateDTO threadDTO) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = ((MyUserDetails) authentication.getPrincipal()).getUsername();
		Optional<ThreadDTO> createdThread = threadService.create(threadDTO, username);
		if (createdThread.isPresent()) {
			log.info(THREAD_CREATED_LOG, createdThread);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdThread.get());
		}
		return ResponseEntity.badRequest().build();
	}
	
	@Operation(summary = "Update a thread by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
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
			@ApiResponse(responseCode = HTTP_NO_CONTENT, content = @Content),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
	@DeleteMapping("/{id}")
	//@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		threadService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}