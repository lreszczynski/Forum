package com.example.demo.posts.dto;

import com.example.demo.posts.validation.CreatePost;
import com.example.demo.posts.validation.UpdatePost;
import com.example.demo.threads.dto.ThreadDTO;
import com.example.demo.users.dto.UserBasicDTO;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.Instant;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDTO {
	@NotNull(groups = UpdatePost.class)
	@Null(groups = CreatePost.class)
	private Long id;
	
	@NotEmpty
	@Length(min = 10, max = 10000)
	private String content;
	
	private Instant createDate;
	
	private ThreadDTO thread;
	
	private UserBasicDTO user;
}
