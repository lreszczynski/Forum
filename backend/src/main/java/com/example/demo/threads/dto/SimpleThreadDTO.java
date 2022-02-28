package com.example.demo.threads.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleThreadDTO {
	private Long id;
	
	private String title;
	
	private Instant createDate;
	
	private boolean active;
}
