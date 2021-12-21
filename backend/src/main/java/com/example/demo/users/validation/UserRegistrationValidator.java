package com.example.demo.users.validation;

import com.example.demo.users.UserRegistrationDTO;
import com.example.demo.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UserRegistrationValidator implements ConstraintValidator<UserRegistrationConstraint, UserRegistrationDTO> {
	UserService userService;
	
	@Autowired
	public UserRegistrationValidator(UserService userService) {
		this.userService = userService;
	}
	
	private String message;
	
	public void initialize(UserRegistrationConstraint constraintAnnotation) {
		this.message = constraintAnnotation.message();
	}
	
	@Override
	public boolean isValid(UserRegistrationDTO userRegistrationDTO, ConstraintValidatorContext cxt) {
		cxt.disableDefaultConstraintViolation();
		
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