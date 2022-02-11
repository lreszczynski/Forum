package com.example.demo.threads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadWithPostsCountDTO {
	private ThreadDTO thread;
	
	private int postsCount;
}
