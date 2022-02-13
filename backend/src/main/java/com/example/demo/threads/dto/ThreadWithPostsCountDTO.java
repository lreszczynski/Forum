package com.example.demo.threads.dto;

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
