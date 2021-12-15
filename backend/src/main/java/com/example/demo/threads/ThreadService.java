package com.example.demo.threads;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ThreadService {
	private final ThreadRepository threadRepository;
	private final ModelMapper modelMapper;
	
	public ThreadService(ThreadRepository threadRepository){
		this.threadRepository = threadRepository;
		this.modelMapper = new ModelMapper();
	}
	
	public List<ThreadDTO> getAll() {
		Type listType = new TypeToken<List<ThreadDTO>>() {
		}.getType();
		return modelMapper.map(threadRepository.findAll(), listType);
	}
	
	public Optional<ThreadDTO> getById(Long id) {
		Optional<Thread> optionalThread = threadRepository.findById(id);
		return optionalThread.map(thread -> modelMapper.map(thread, ThreadDTO.class));
	}
	
	public ThreadDTO create(ThreadDTO threadDTO) {
		Thread thread = modelMapper.map(threadDTO, Thread.class);
		Thread saved = threadRepository.save(thread);
		return modelMapper.map(saved, ThreadDTO.class);
	}
	
	public void deleteById(Long id) {
		threadRepository.deleteById(id);
	}
	
	public Optional<ThreadDTO> update(Long id, ThreadDTO threadDTO) {
		if (!threadDTO.getId().equals(id)) {
			return Optional.empty();
		}
		Thread thread = modelMapper.map(threadDTO, Thread.class);
		Thread saved = threadRepository.save(thread);
		
		return Optional.of(modelMapper.map(saved, ThreadDTO.class));
	}
	
}
