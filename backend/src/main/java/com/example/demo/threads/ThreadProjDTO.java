package com.example.demo.threads;

import com.example.demo.posts.PostWithoutContentDTO;
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
