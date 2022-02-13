package com.example.demo.posts.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostCreateDTO {
	@Length(min = 10, max = 10000)
	private String content;
	
	@NotNull
	private Long threadId;
}
