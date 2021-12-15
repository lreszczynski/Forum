package com.example.demo.validation;

import com.example.demo.user.UserRegistrationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PasswordValidationTest {
	
	@Autowired
	private LocalValidatorFactoryBean validator;
	
	@BeforeEach
	public void setUp() {
	}
	
	@Test
	public void passwordShouldPassValidationIfItIsStrongEnough() {
		UserRegistrationDTO userRegistrationDTO = UserRegistrationDTO.builder()
				.username("username")
				.password("Password1!")
				.email("test@gmail.com")
				.build();
		Set<ConstraintViolation<UserRegistrationDTO>> constraintViolations = validator.validate(userRegistrationDTO);
		
		assertThat(constraintViolations.size()).isEqualTo(0);
	}
	
	@Test
	public void passwordShouldNotPassValidationIfItDoesNotContainAtLeast8Characters() {
		UserRegistrationDTO userRegistrationDTO = UserRegistrationDTO.builder()
				.username("username")
				.password("Pass1!")
				.email("test@gmail.com")
				.build();
		Set<ConstraintViolation<UserRegistrationDTO>> constraintViolations = validator.validate(userRegistrationDTO);
		
		assertThat(constraintViolations.size()).isGreaterThan(0);
	}
	
	@Test
	public void passwordShouldNotPassValidationIfItDoesNotContainAtLeastOneUppercaseCharacter() {
		UserRegistrationDTO userRegistrationDTO = UserRegistrationDTO.builder()
				.username("username")
				.password("password!1")
				.email("test@gmail.com")
				.build();
		Set<ConstraintViolation<UserRegistrationDTO>> constraintViolations = validator.validate(userRegistrationDTO);
		
		assertThat(constraintViolations.size()).isGreaterThan(0);
	}
	
	@Test
	public void passwordShouldNotPassValidationIfItDoesNotContainAtLeastOneLowercaseCharacter() {
		UserRegistrationDTO userRegistrationDTO = UserRegistrationDTO.builder()
				.username("username")
				.password("PASSWORD1!")
				.email("test@gmail.com")
				.build();
		Set<ConstraintViolation<UserRegistrationDTO>> constraintViolations = validator.validate(userRegistrationDTO);
		
		assertThat(constraintViolations.size()).isGreaterThan(0);
	}
	
	@Test
	public void passwordShouldNotPassValidationIfItDoesNotContainAtLeastOneNumber() {
		UserRegistrationDTO userRegistrationDTO = UserRegistrationDTO.builder()
				.username("username")
				.password("Password!")
				.email("test@gmail.com")
				.build();
		Set<ConstraintViolation<UserRegistrationDTO>> constraintViolations = validator.validate(userRegistrationDTO);
		
		assertThat(constraintViolations.size()).isGreaterThan(0);
	}
	
	@Test
	public void passwordShouldNotPassValidationIfItDoesNotContainAtLeastOneSpecialCharacter() {
		UserRegistrationDTO userRegistrationDTO = UserRegistrationDTO.builder()
				.username("username")
				.password("Password1")
				.email("test@gmail.com")
				.build();
		Set<ConstraintViolation<UserRegistrationDTO>> constraintViolations = validator.validate(userRegistrationDTO);
		
		assertThat(constraintViolations.size()).isGreaterThan(0);
	}
	
}