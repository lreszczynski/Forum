package com.example.demo.posts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostCreateDTO {
	@Length(min = 10, max = 10000)
	private String content;
	
	@NotNull
	private Long threadId;
}
