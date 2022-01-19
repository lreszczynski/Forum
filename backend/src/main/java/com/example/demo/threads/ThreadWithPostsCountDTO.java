package com.example.demo.threads;

import com.example.demo.categories.CategoryDTO;
import com.example.demo.posts.PostDTO;
import com.example.demo.users.UserDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadWithPostsCountDTO {
	private Long id;
	
	private String title;
	
	private boolean active;
	
	@JsonBackReference
	private CategoryDTO category;
	
	private UserDTO user;
	
	private int postsCount;
}
