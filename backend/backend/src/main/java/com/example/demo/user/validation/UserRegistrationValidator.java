package com.example.demo.user.validation;

import com.example.demo.user.UserRegistrationDTO;
import com.example.demo.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UserRegistrationValidator implements ConstraintValidator<UserRegistrationConstraint, UserRegistrationDTO> {
	UserService userService;
	
	@Autowired
	public UserRegistrationValidator(UserService userService) {
		this.userService = userService;
	}
	
	public UserRegistrationValidator() {
	}
	
	private String message;
	
	public void initialize(UserRegistrationConstraint constraintAnnotation) {
		this.message = constraintAnnotation.message();
	}
	
	@Override
	public boolean isValid(UserRegistrationDTO userRegistrationDTO, ConstraintValidatorContext cxt) {
		cxt.disableDefaultConstraintViolation();
		
		// repository.save(object) uses validators under the hood (if available), but those are unable to autowire repositories.
		// The @Valid annotation calls validator that is capable of autowiring services and repositories.
		if (userService == null)
			return true;
		
		if (userService.existsUserByEmail(userRegistrationDTO.getEmail())) {
			cxt.buildConstraintViolationWithTemplate("Email is not unique").addPropertyNode("email").addConstraintViolation();
			return false;
		} else if (userService.existsUserByUsername(userRegistrationDTO.getUsername())) {
			cxt.buildConstraintViolationWithTemplate("Username is not unique").addPropertyNode("username").addConstraintViolation();
			return false;
		}
		return true;
	}
	
}