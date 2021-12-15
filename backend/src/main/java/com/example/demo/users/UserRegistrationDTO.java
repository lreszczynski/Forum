package com.example.demo.users;

import com.example.demo.users.validation.PasswordConstraint;
import com.example.demo.users.validation.UserRegistrationConstraint;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@UserRegistrationConstraint
public class UserRegistrationDTO {
	@Length(min = 4, max = 40)
	@JsonProperty("username")
	private String username;
	
	@PasswordConstraint
	@JsonProperty(value = "password")
	private String password;
	
	@Length(min = 3, max = 254)
	@JsonProperty("email")
	private String email;
}
