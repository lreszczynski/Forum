package com.example.demo.threads;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThreadCreateDTO {
	@NotEmpty
	@Length(max = 80)
	private String title;
	
	@NotEmpty
	@Length(min = 10, max = 10000)
	private String content;
	
	@NotNull
	private Long categoryId;
}
