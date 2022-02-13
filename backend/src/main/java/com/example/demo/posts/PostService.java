package com.example.demo.posts;

import com.example.demo.posts.dto.PostCreateDTO;
import com.example.demo.posts.dto.PostDTO;
import com.example.demo.threads.Thread;
import com.example.demo.threads.ThreadRepository;
import com.example.demo.users.User;
import com.example.demo.users.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
public class PostService {
	private final PostRepository postRepository;
	private final ThreadRepository threadRepository;
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	
	public PostService(PostRepository postRepository, ThreadRepository threadRepository,
	                   UserRepository userRepository) {
		this.postRepository = postRepository;
		this.threadRepository = threadRepository;
		this.userRepository = userRepository;
		this.modelMapper = new ModelMapper();
	}
	
	public Page<PostDTO> getAll(Pageable pageable) {
		Page<Thread> page = threadRepository.findAll(pageable);
		return page.map(thread -> modelMapper.map(thread, PostDTO.class));
	}
	
	public Page<PostDTO> findAllByContentContaining(String text, Pageable pageable) {
		Page<Post> allByContentLike = postRepository.findAllByContentContainingIgnoreCase(text, pageable);
		return allByContentLike.map(post -> modelMapper.map(post, PostDTO.class));
	}
	
	public Page<PostDTO> getAllByThreadId(Long id, Pageable pageable) {
		Page<Post> allByThreadId = postRepository.findAllByThreadIdOrderById(id, pageable);
		return allByThreadId.map(post -> modelMapper.map(post, PostDTO.class));
	}
	
	public Optional<PostDTO> getById(Long id) {
		Optional<Post> optionalPost = postRepository.findById(id);
		return optionalPost.map(post -> modelMapper.map(post, PostDTO.class));
	}
	
	public Optional<PostDTO> create(PostCreateDTO postCreateDTO, String username) {
		Optional<Thread> optionalThread = threadRepository.findById(postCreateDTO.getThreadId());
		Optional<User> optionalUser = userRepository.findByUsername(username);
		if (optionalThread.isPresent() && optionalUser.isPresent()) {
			Post post = Post.builder()
					.content(postCreateDTO.getContent())
					.user(optionalUser.get())
					.thread(optionalThread.get())
					.createDate(Instant.now())
					.build();
			Post saved = postRepository.save(post);
			return Optional.of(modelMapper.map(saved, PostDTO.class));
		}
		return Optional.empty();
	}
	
	public void deleteById(Long id) {
		postRepository.deleteById(id);
	}
	
	public Optional<PostDTO> update(Long id, PostDTO postDTO) {
		Optional<Post> optionalPostDTO = postRepository.findById(id);
		
		if (optionalPostDTO.isPresent()) {
			Post post = optionalPostDTO.get();
			post.setContent(postDTO.getContent());
			Post saved = postRepository.save(post);
			return Optional.of(modelMapper.map(saved, PostDTO.class));
		}
		return Optional.empty();
	}
	
}
