package com.example.demo.users;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginDTO {
	@JsonProperty("username")
	private String username;
	
	@JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
	private String password;
}
