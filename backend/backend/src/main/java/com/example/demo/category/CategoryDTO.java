package com.example.demo.category;

import com.example.demo.category.validation.CategoryUniqueConstraint;
import com.example.demo.category.validation.CreateCategory;
import com.example.demo.category.validation.SecondOrder;
import com.example.demo.category.validation.UpdateCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@CategoryUniqueConstraint(groups = SecondOrder.class)
@GroupSequence({CategoryDTO.class, SecondOrder.class})
public class CategoryDTO {
	@NotNull(groups = UpdateCategory.class)
	@Null(groups = CreateCategory.class)
	@JsonProperty("id")
	private Long id;
	
	@NotEmpty
	@Length(max = 50)
	@JsonProperty("name")
	private String name;
	
	@NotNull
	@Length(max = 250, min = 5)
	@JsonProperty("description")
	private String description;
	
	@NotNull
	@JsonProperty("active")
	private boolean active;
}