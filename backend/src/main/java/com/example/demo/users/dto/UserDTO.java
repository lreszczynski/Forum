package com.example.demo.users.dto;

import com.example.demo.roles.dto.RoleDTO;
import lombok.*;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
	private Long id;
	
	private String username;
	
	private String email;
	
	private boolean banned;
	
	private boolean active;
	
	private RoleDTO role;
}
