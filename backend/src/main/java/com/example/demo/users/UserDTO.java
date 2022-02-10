package com.example.demo.users;

import com.example.demo.roles.RoleDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
	private Long id;
	
	private String username;
	
	private String email;
	
	private boolean banned;
	
	private boolean active;
	
	//@EqualsAndHashCode.Exclude
	private RoleDTO role;
}
