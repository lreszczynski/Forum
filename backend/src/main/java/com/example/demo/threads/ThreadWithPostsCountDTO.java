package com.example.demo.threads;

import com.example.demo.categories.CategoryDTO;
import com.example.demo.users.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadWithPostsCountDTO {
	private Long id;
	
	private String title;
	
	private boolean active;
	
	private boolean pinned;
	
	private Instant createDate;
	
	private UserDTO user;
	
	private CategoryDTO category;
	
	private int postsCount;
}
