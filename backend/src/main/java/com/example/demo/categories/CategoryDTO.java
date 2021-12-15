package com.example.demo.categories;

import com.example.demo.categories.validation.CategoryUniqueConstraint;
import com.example.demo.categories.validation.CreateCategory;
import com.example.demo.categories.validation.SecondOrder;
import com.example.demo.categories.validation.UpdateCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
	@Length(min = 5, max = 250)
	@JsonProperty("description")
	private String description;
	
	@NotNull
	@JsonProperty("active")
	private boolean active;
}