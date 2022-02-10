package com.example.demo.threads;

import com.example.demo.categories.CategoryDTO;
import com.example.demo.posts.PostDTO;
import com.example.demo.users.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadWithPostsDTO {
	private Long id;
	
	private String title;
	
	private Instant createDate;
	
	private boolean active;
	
	private CategoryDTO category;
	
	private UserDTO user;
	
	private List<PostDTO> posts;
}
