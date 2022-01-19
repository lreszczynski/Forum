package com.example.demo.threads;

import com.example.demo.posts.PostDTO;
import com.example.demo.users.UserDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadWithoutCategoryDTO {
	private Long id;
	
	private String title;
	
	private boolean active;
	
	private UserDTO user;
	
	@JsonProperty("posts-count")
	private int postsCount;
	
	@JsonProperty("last-post")
	private PostDTO lastPost;
}
