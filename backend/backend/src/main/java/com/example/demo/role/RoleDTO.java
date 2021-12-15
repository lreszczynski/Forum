package com.example.demo.role;

import com.example.demo.role.validation.CreateRole;
import com.example.demo.role.validation.RoleUniqueConstraint;
import com.example.demo.role.validation.SecondOrder;
import com.example.demo.role.validation.UpdateRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RoleUniqueConstraint(groups = SecondOrder.class)
@GroupSequence({RoleDTO.class, SecondOrder.class})
public class RoleDTO {
	@NotNull(groups = UpdateRole.class)
	@Null(groups = CreateRole.class)
	@JsonProperty("id")
	private Long id;
	
	@NotEmpty
	@Length(min = 2, max = 50)
	@JsonProperty("name")
	private String name;
	
	@NotNull
	@Length(max = 250)
	@JsonProperty("description")
	private String description;
}
