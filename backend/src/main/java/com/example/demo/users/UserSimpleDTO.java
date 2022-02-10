package com.example.demo.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleDTO {
	private Long id;
	
	private String username;
	
	private boolean banned;
}
