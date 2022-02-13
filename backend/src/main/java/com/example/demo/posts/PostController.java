package com.example.demo.posts;

import com.example.demo.posts.dto.PostCreateDTO;
import com.example.demo.posts.dto.PostDTO;
import com.example.demo.posts.validation.CreatePost;
import com.example.demo.posts.validation.UpdatePost;
import com.example.demo.security.MyUserDetails;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
import java.util.Optional;

import static com.example.demo.controller.HttpResponse.*;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(SecurityUtility.POSTS_PATH)
@Slf4j
public class PostController {
	private static final String POST_CREATED_LOG = "New post was created: {}";
	private static final String POST_UPDATED_LOG = "Post was updated: {}";
	
	private final PostService postService;
	
	@Autowired
	public PostController(PostService postService) {
		this.postService = postService;
	}
	
	@Operation(summary = "Returns a list of posts")
	@ApiResponse(responseCode = HTTP_OK)
	@PageableAsQueryParam
	@GetMapping
	ResponseEntity<Page<PostDTO>> getAll(@Parameter(hidden = true) Pageable pageable) {
		Page<PostDTO> posts = postService.getAll(pageable);
		return ResponseEntity.ok(posts);
	}
	
	@Operation(summary = "Returns a list of posts containing the search term")
	@ApiResponse(responseCode = HTTP_OK)
	@PageableAsQueryParam
	@GetMapping("/search")
	ResponseEntity<Page<PostDTO>> searchForText(@RequestParam("text") String text, @Parameter(hidden = true) Pageable pageable) {
		Page<PostDTO> posts = postService.findAllByContentContaining(text, pageable);
		return ResponseEntity.ok(posts);
	}
	
	@Operation(summary = "Get a post by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content)})
	@GetMapping("/{id}")
	ResponseEntity<PostDTO> getById(@PathVariable long id) {
		Optional<PostDTO> postDTO = postService.getById(id);
		if (postDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(postDTO.get());
	}
	
	@Operation(summary = "Create a new post")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_CREATED),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content)})
	@PreAuthorize("@roleContainer.canCreatePost(principal, #postCreateDTO.threadId)")
	@PostMapping
	ResponseEntity<PostDTO> create(
			@Validated({Default.class, CreatePost.class}) @RequestBody PostCreateDTO postCreateDTO) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = ((MyUserDetails) authentication.getPrincipal()).getUsername();
		Optional<PostDTO> createdPost = postService.create(postCreateDTO, username);
		if (createdPost.isPresent()) {
			log.info(POST_CREATED_LOG, createdPost);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdPost.get());
		}
		return ResponseEntity.badRequest().build();
	}
	
	@Operation(summary = "Update a post by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, content = @Content),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
	@PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
	@PreAuthorize("@roleContainer.canEditPost(principal, #postDTO.id)")
	ResponseEntity<PostDTO> update(@Validated({Default.class, UpdatePost.class}) @RequestBody PostDTO postDTO,
	                               @PathVariable Long id) {
		Optional<PostDTO> updatedPost = postService.update(id, postDTO);
		if (updatedPost.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		log.info(POST_UPDATED_LOG, updatedPost);
		return ResponseEntity.ok(updatedPost.get());
	}
	
	@Operation(summary = "Delete a post")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_NO_CONTENT, content = @Content),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, content = @Content)})
	@DeleteMapping("/{id}")
	@PreAuthorize("@roleContainer.canEditPost(principal, #id)")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		postService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}