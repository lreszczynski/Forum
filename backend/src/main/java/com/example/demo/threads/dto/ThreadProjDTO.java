package com.example.demo.threads.dto;

import com.example.demo.posts.dto.PostWithoutContentDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadProjDTO {
	private ThreadDTO thread;
	private PostWithoutContentDTO lastPost;
	private int postsCount;
}
