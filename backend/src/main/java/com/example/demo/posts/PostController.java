package com.example.demo.posts;

import com.example.demo.posts.validation.CreatePost;
import com.example.demo.posts.validation.UpdatePost;
import com.example.demo.security.SecurityUtility;
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
@RequestMapping(SecurityUtility.POSTS_PATH)
@Slf4j
public class PostController {
	// TODO @ApiResponse @Schema DTO
	private static final String POST_CREATED_LOG = "New post was created: {}";
	private static final String POST_UPDATED_LOG = "Post was updated: {}";
	
	private final PostService postService;
	
	@Autowired
	public PostController(PostService postService) {
		this.postService = postService;
	}
	
	@Operation(summary = "Returns a list of posts")
	@ApiResponse(responseCode = "200", description = "List was returned", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Post.class))})
	@GetMapping("/")
	ResponseEntity<Collection<PostDTO>> getAll() {
		List<PostDTO> posts = postService.getAll();
		return ResponseEntity.ok(posts);
	}
	
	@Operation(summary = "Get a post by its id")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the post", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Post.class))}),
			@ApiResponse(responseCode = "404", description = "Post not found", content = @Content)})
	@GetMapping("/{id}")
	//@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	ResponseEntity<PostDTO> getById(@PathVariable long id) {
		Optional<PostDTO> postDTO = postService.getById(id);
		if (postDTO.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(postDTO.get());
	}
	
	@Operation(summary = "Create a new post")
	@ApiResponse(responseCode = "201", description = "Post is created", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Post.class))})
	@PostMapping(value = "/", consumes = APPLICATION_JSON_VALUE)
	//@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	ResponseEntity<PostDTO> create(@Validated({Default.class, CreatePost.class}) @RequestBody PostDTO postDTO) {
		PostDTO createdPost = postService.create(postDTO);
		log.info(POST_CREATED_LOG, createdPost);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
	}
	
	@Operation(summary = "Update a post by its id")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Post was updated", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Post.class))}),
			@ApiResponse(responseCode = "404", description = "Post not found", content = @Content)})
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
	@ApiResponse(responseCode = "204", description = "Post was deleted", content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Post.class))})
	@DeleteMapping("/{id}")
	//@PreAuthorize("@roleContainer.isAtLeastModerator(principal)")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		postService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}