package com.example.demo.user;

import com.example.demo.role.Role;
import com.example.demo.user.validation.CreateUser;
import com.example.demo.user.validation.UpdateUser;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
	@NotNull(groups = UpdateUser.class)
	@Null(groups = CreateUser.class)
	@JsonProperty("id")
	private Long id;
	
	@Length(min = 4, max = 40)
	@JsonProperty("username")
	private String username;
	
	@Length(min = 60, max = 60)
	@JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
	private String password;
	
	@Length(min = 3, max = 254)
	@JsonProperty("email")
	private String email;
	
	@JsonProperty("active")
	private boolean active;
	
	@JsonProperty(value = "role", access = JsonProperty.Access.READ_ONLY)
	private Role role;
}
