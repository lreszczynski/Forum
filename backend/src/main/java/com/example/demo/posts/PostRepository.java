package com.example.demo.posts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
	Post findTopByThread_IdOrderByIdDesc(Long threadId);
	
	List<Post> findAllByThreadIdOrderByIdAsc(Long threadId);
	
	Long countAllByThreadId(Long threadId);
	
	Page<Post> findAllByThreadIdOrderById(Long threadId, Pageable pageable);
	
	Page<Post> findAllByContentContainingIgnoreCase(String text, Pageable pageable);
	
}