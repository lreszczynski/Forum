package com.example.demo.posts;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PostService {
	private final PostRepository postRepository;
	private final ModelMapper modelMapper;
	
	public PostService(PostRepository postRepository){
		this.postRepository = postRepository;
		this.modelMapper = new ModelMapper();
	}
	
	public List<PostDTO> getAll() {
		Type listType = new TypeToken<List<PostDTO>>() {
		}.getType();
		return modelMapper.map(postRepository.findAll(), listType);
	}
	
	public Optional<PostDTO> getById(Long id) {
		Optional<Post> optionalPost = postRepository.findById(id);
		return optionalPost.map(post -> modelMapper.map(post, PostDTO.class));
	}
	
	public PostDTO create(PostDTO postDTO) {
		Post post = modelMapper.map(postDTO, Post.class);
		Post saved = postRepository.save(post);
		return modelMapper.map(saved, PostDTO.class);
	}
	
	public void deleteById(Long id) {
		postRepository.deleteById(id);
	}
	
	public Optional<PostDTO> update(Long id, PostDTO postDTO) {
		if (!postDTO.getId().equals(id)) {
			return Optional.empty();
		}
		Post post = modelMapper.map(postDTO, Post.class);
		Post saved = postRepository.save(post);
		
		return Optional.of(modelMapper.map(saved, PostDTO.class));
	}
	
}
