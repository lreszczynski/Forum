package com.example.demo.threads.dto;

import com.example.demo.categories.dto.CategoryDTO;
import com.example.demo.threads.validation.CreateThread;
import com.example.demo.threads.validation.SecondOrder;
import com.example.demo.threads.validation.UpdateThread;
import com.example.demo.users.dto.UserDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.Instant;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
	
	@JsonProperty("createDate")
	private Instant createDate;
	
	@JsonProperty("active")
	private boolean active;
	
	@JsonProperty("pinned")
	private boolean pinned;
	
	@JsonProperty(value = "category")
	private CategoryDTO category;
	
	@JsonProperty(value = "user")
	private UserDTO user;
}
