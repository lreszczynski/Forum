package com.example.demo.threads;

import com.example.demo.categories.Category;
import com.example.demo.categories.CategoryRepository;
import com.example.demo.posts.Post;
import com.example.demo.posts.PostRepository;
import com.example.demo.users.User;
import com.example.demo.users.UserRepository;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ThreadService {
	private final ThreadRepository threadRepository;
	private final PostRepository postRepository;
	private final CategoryRepository categoryRepository;
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	
	public ThreadService(ThreadRepository threadRepository, PostRepository postRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
		this.threadRepository = threadRepository;
		this.postRepository = postRepository;
		this.categoryRepository = categoryRepository;
		this.userRepository = userRepository;
		this.modelMapper = new ModelMapper();
		TypeMap<Thread, ThreadWithPostsCountDTO> propertyMapper = modelMapper.createTypeMap(Thread.class, ThreadWithPostsCountDTO.class);
		Converter<Collection<Post>, Integer> collectionToSize = (c -> c.getSource().size());
		propertyMapper.addMappings(mapper -> mapper.using(collectionToSize).map(Thread::getPosts, ThreadWithPostsCountDTO::setPostsCount));
	}
	
	public List<ThreadWithPostsCountDTO> getAll() {
		Type listType = new TypeToken<List<ThreadWithPostsCountDTO>>() {
		}.getType();
		return modelMapper.map(threadRepository.findAll(), listType);
	}
	
	public List<ThreadProjDTO> getAllPinnedByCategoryId(Long id) {
		List<ThreadProjDTO> list = threadRepository.findAllByCategoryIdAndPinnedIsTrue(id)
				.stream().map(thread -> modelMapper.map(thread, ThreadProjDTO.class)).collect(Collectors.toList());
		list.forEach(threadWithLastPostDTO -> {
			threadWithLastPostDTO.setPostsCount(Math.toIntExact(postRepository.countAllByThreadId(threadWithLastPostDTO.getThread().getId())));
		});
		return list;
	}
	
	public Page<ThreadProjDTO> getAllByCategoryId(Long id, Pageable pageable) {
		Type type = new TypeToken<Page<ThreadProjDTO>>() {
		}.getType();
		Page<TestSimpleProj> eeeee = threadRepository.findAllByCategoryIdAndPinnedIsFalse(id, pageable);
		Page<ThreadProjDTO> list = threadRepository.findAllByCategoryIdAndPinnedIsFalse(id, pageable)
				.map(thread -> modelMapper.map(thread, ThreadProjDTO.class));
		
		list.forEach(threadWithLastPostDTO -> {
			threadWithLastPostDTO.setPostsCount(Math.toIntExact(postRepository.countAllByThreadId(threadWithLastPostDTO.getThread().getId())));
		});
		return list;
	}
	
	public Optional<ThreadDTO> findById(Long id) {
		Optional<Thread> optionalThread = threadRepository.findById(id);
		return optionalThread.map(thread -> modelMapper.map(thread, ThreadDTO.class));
	}
	
	public Optional<ThreadDTO> create(ThreadCreateDTO threadCreateDTO, String username) {
		Optional<User> optionalUser = userRepository.findByUsername(username);
		Optional<Category> optionalCategory = categoryRepository.findById(threadCreateDTO.getCategoryId());
		if (optionalUser.isPresent() && optionalCategory.isPresent()) {
			User user = optionalUser.get();
			Category category = optionalCategory.get();
			Thread thread = Thread.builder().title(threadCreateDTO.getTitle()).category(category).createDate(Instant.now()).active(true).pinned(false).user(user).build();
			Thread savedThread = threadRepository.save(thread);
			Post post = Post.builder().content(threadCreateDTO.getContent()).user(user).thread(savedThread).createDate(Instant.now()).build();
			Post savedPost = postRepository.save(post);
			return Optional.of(modelMapper.map(savedThread, ThreadDTO.class));
		}
		return Optional.empty();
	}
	
	public void deleteById(Long id) {
		threadRepository.deleteById(id);
	}
	
	public Optional<ThreadDTO> update(Long id, ThreadDTO threadDTO) {
		
		Optional<Thread> optionalCategory = threadRepository.findById(id).map(thread -> {
			thread.setTitle(threadDTO.getTitle());
			thread.setActive(threadDTO.isActive());
			thread.setPinned(threadDTO.isPinned());
			categoryRepository.findById(threadDTO.getCategory().getId()).ifPresent(thread::setCategory);
			return threadRepository.save(thread);
		});
		return optionalCategory.map(thread -> modelMapper.map(thread, ThreadDTO.class));
	}
	
}
