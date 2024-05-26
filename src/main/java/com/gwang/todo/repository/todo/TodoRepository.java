package com.gwang.todo.repository.todo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gwang.todo.model.todo.Todo;


@Repository
public interface TodoRepository extends JpaRepository<Todo, Integer> {
		
	public List<Todo> findByUserId(Integer userId);
	public Page<Todo> findByUserId(Integer userId, Pageable pageable);
	Optional<Todo> findTopByUserIdOrderByCreatedAtDesc(Integer userId);
	Page<Todo> findByTaskStatusAndUserId(Integer taskStatus, Integer userId, Pageable pageable);
	
}
