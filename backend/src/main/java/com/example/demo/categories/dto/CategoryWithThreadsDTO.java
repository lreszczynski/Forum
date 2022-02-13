package com.example.demo.categories.dto;

import com.example.demo.threads.dto.ThreadWithLastPostDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class CategoryWithThreadsDTO {
	private Long id;
	
	private String name;
	
	private String description;
	
	private boolean active;
	
	// @JsonManagedReference
	private List<ThreadWithLastPostDTO> threads;
}