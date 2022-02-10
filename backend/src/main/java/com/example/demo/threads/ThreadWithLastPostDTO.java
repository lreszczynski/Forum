package com.example.demo.threads;

import com.example.demo.posts.PostWithoutContentDTO;
import com.example.demo.users.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadWithLastPostDTO {
	private Long id;
	
	private String title;
	
	private boolean active;
	
	private boolean pinned;
	
	private Instant createDate;
	
	private UserDTO user;
	
	private int postsCount;
	
	private PostWithoutContentDTO lastPost;
}
