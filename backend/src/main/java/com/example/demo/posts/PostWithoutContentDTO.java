package com.example.demo.posts;

import com.example.demo.users.UserSimpleDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostWithoutContentDTO {
	private Long id;
	
	private Instant createDate;
	
	private UserSimpleDTO user;
}
