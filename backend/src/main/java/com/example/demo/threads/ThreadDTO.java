package com.example.demo.threads;

import com.example.demo.categories.CategoryDTO;
import com.example.demo.roles.validation.RoleUniqueConstraint;
import com.example.demo.threads.validation.CreateThread;
import com.example.demo.threads.validation.SecondOrder;
import com.example.demo.threads.validation.UpdateThread;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RoleUniqueConstraint(groups = SecondOrder.class)
@GroupSequence({ThreadDTO.class, SecondOrder.class})
public class ThreadDTO {
	@NotNull(groups = UpdateThread.class)
	@Null(groups = CreateThread.class)
	@JsonProperty("id")
	private Long id;
	
	@NotEmpty
	@Length(max = 80)
	@JsonProperty("title")
	private String title;
	
	@JsonProperty("active")
	private boolean active;
	
	@JsonProperty(value = "category", access = JsonProperty.Access.READ_ONLY)
	private CategoryDTO category;
	
	@JsonProperty(value = "user", access = JsonProperty.Access.READ_ONLY)
	private UserDTO user;
	
	
}
