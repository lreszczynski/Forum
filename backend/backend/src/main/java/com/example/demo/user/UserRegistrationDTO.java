package com.example.demo.user;

import com.example.demo.user.validation.PasswordConstraint;
import com.example.demo.user.validation.UserRegistrationConstraint;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@UserRegistrationConstraint
public class UserRegistrationDTO {
	@Length(min = 4, max = 40)
	@JsonProperty("username")
	private String username;
	
	@PasswordConstraint
	@JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
	private String password;
	
	@Length(min = 3, max = 254)
	@JsonProperty("email")
	private String email;
}
