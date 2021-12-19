package com.example.demo.posts;

import com.example.demo.categories.CategoryDTO;
import com.example.demo.posts.validation.CreatePost;
import com.example.demo.posts.validation.UpdatePost;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
import java.util.Collection;
import java.util.List;
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
	@ApiResponse(responseCode = HTTP_OK, description = HTTP_OK_MESSAGE, content = {
			@Content(mediaType = APPLICATION_JSON_VALUE, array =
			@ArraySchema(schema = @Schema(implementation = PostDTO.class)))})
	@GetMapping
	ResponseEntity<Collection<PostDTO>> getAll() {
		List<PostDTO> posts = postService.getAll();
		return ResponseEntity.ok(posts);
	}
	
	@Operation(summary = "Get a post by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK, description = HTTP_OK_MESSAGE, content = {
					@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = PostDTO.class))}),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, description = HTTP_NOT_FOUND_MESSAGE)})
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
			@ApiResponse(responseCode = HTTP_CREATED, description = HTTP_CREATED_MESSAGE, content = {
					@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = PostDTO.class))}),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, description = HTTP_BAD_REQUEST_MESSAGE),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, description = HTTP_FORBIDDEN_MESSAGE),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = HTTP_UNAUTHORIZED_MESSAGE),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, description = HTTP_NOT_FOUND_MESSAGE)})
	@PreAuthorize("@roleContainer.isNotBanned(principal)")
	ResponseEntity<PostDTO> create(@Validated({Default.class, CreatePost.class}) @RequestBody PostDTO postDTO) {
		PostDTO createdPost = postService.create(postDTO);
		log.info(POST_CREATED_LOG, createdPost);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
	}
	
	@Operation(summary = "Update a post by its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_OK, description = HTTP_OK_MESSAGE, content = {
					@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoryDTO.class))}),
			@ApiResponse(responseCode = HTTP_NOT_FOUND, description = HTTP_NOT_FOUND_MESSAGE, content = @Content),
			@ApiResponse(responseCode = HTTP_BAD_REQUEST, description = HTTP_BAD_REQUEST_MESSAGE),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, description = HTTP_FORBIDDEN_MESSAGE),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = HTTP_UNAUTHORIZED_MESSAGE)})
	@PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
	//@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	ResponseEntity<PostDTO> update(@Validated({Default.class, UpdatePost.class}) @RequestBody PostDTO postDTO, @PathVariable Long id) {
		Optional<PostDTO> updatedPost = postService.update(id, postDTO);
		if (updatedPost.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		log.info(POST_UPDATED_LOG, updatedPost);
		return ResponseEntity.ok(updatedPost.get());
	}
	
	@Operation(summary = "Delete a post")
	@ApiResponses(value = {
			@ApiResponse(responseCode = HTTP_NO_CONTENT, description = HTTP_NO_CONTENT_MESSAGE),
			@ApiResponse(responseCode = HTTP_FORBIDDEN, description = HTTP_FORBIDDEN_MESSAGE),
			@ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = HTTP_UNAUTHORIZED_MESSAGE)})
	@DeleteMapping("/{id}")
	//@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		postService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}