package com.example.demo.posts.dto;

import com.example.demo.users.dto.UserBasicDTO;
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
	
	private UserBasicDTO user;
}
