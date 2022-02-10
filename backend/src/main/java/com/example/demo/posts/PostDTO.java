package com.example.demo.posts;

import com.example.demo.posts.validation.CreatePost;
import com.example.demo.posts.validation.UpdatePost;
import com.example.demo.threads.ThreadDTO;
import com.example.demo.users.UserDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.Instant;

@Data
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
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private UserDTO user;
}
