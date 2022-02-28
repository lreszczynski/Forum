package com.example.demo.categories.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class SimpleCategoryDTO {
	private Long id;
	
	private String name;
	
	private String description;
	
	private boolean active;
}