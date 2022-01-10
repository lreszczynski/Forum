package com.example.demo.posts;

import com.example.demo.posts.validation.CreatePost;
import com.example.demo.posts.validation.SecondOrder;
import com.example.demo.posts.validation.UpdatePost;
import com.example.demo.roles.validation.RoleUniqueConstraint;
import com.example.demo.users.UserDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RoleUniqueConstraint(groups = SecondOrder.class)
@GroupSequence({PostDTO.class, SecondOrder.class})
public class PostDTO {
	@NotNull(groups = UpdatePost.class)
	@Null(groups = CreatePost.class)
	@JsonProperty("id")
	private Long id;
	
	@NotEmpty
	@Length(min = 10, max = 10000)
	@JsonProperty("content")
	private String content;
	
	@JsonProperty("create_date")
	private Timestamp createDate;
	
	@JsonProperty(value = "user", access = JsonProperty.Access.READ_ONLY)
	private UserDTO user;
}
