package com.example.demo.posts;

import com.example.demo.users.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostWithoutContentProjection {
	private Long id;
	
	private Timestamp createDate;
	
	private UserDTO user;
}
