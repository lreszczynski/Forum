package com.example.demo.users;

import com.example.demo.users.validation.PasswordConstraint;
import com.example.demo.users.validation.UserRegistrationConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@UserRegistrationConstraint
public class UserRegistrationDTO {
	@Length(min = 4, max = 40)
	private String username;
	
	@PasswordConstraint
	private String password;
	
	@Length(min = 3, max = 254)
	private String email;
}
