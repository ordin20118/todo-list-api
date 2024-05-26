package com.gwang.todo.service;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;

import com.gwang.todo.model.todo.Todo;
import com.gwang.todo.model.todo.TodoDto;
import com.gwang.todo.model.user.User;
import com.gwang.todo.repository.todo.TodoRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TodoService {
	
	@Autowired
	TodoRepository todoRepository;
	
	private TodoDto todoEntityToDto(Todo entity){
		TodoDto dto = TodoDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .todo(entity.getTodo())
                .state(entity.getState())
                .taskStatus(entity.getTaskStatus())
                .weight(entity.getWeight())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .finishedAt(entity.getFinishedAt())
                .build();

        return dto;
    }
	
	private Todo todoDtoToEntity(TodoDto dto){
		Todo entity = Todo.builder()
                .id(dto.getId())
                .todo(dto.getTodo())
                .state(dto.getState())
                .taskStatus(dto.getTaskStatus())
                .weight(dto.getWeight())
                .build();
		entity.setUser(new User(dto.getUserId()));
        return entity;
    }
	

	public TodoDto getTodo(Integer id) {
	    Optional<Todo> result = todoRepository.findById(id);
        return result.isPresent() ? todoEntityToDto(result.get()) : null;
	}
	
	public TodoDto getLatestTodo(Integer userId) {
	    Optional<Todo> result = todoRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        return result.isPresent() ? todoEntityToDto(result.get()) : null;
	}
	
	/**
	 * 할 일 목록 조회
	 * @return
	 * @throws Exception
	 */
	public Page<TodoDto> getList(Integer taskStatus, Integer userId, Pageable pageable) {
		 Page<Todo> todos = getTodoList(taskStatus, userId, pageable);
		 return todos.map(this::todoEntityToDto);
    }
	
	private Page<Todo> getTodoList(Integer taskStatus, Integer userId, Pageable pageable) {
        if (taskStatus == null) { 
            return todoRepository.findByUserId(userId, pageable);
		} else {
            return todoRepository.findByTaskStatusAndUserId(taskStatus, userId, pageable);
        }
    }
	
	
	/**
	 * 할 일 저장
	 * @return
	 * @throws Exception
	 */
	public Integer saveTodo(TodoDto todo) throws Exception {
		try {
			log.info("New todo:" + todo);
			if(!todo.isValidate()) {
				throw new Exception("Bad Request.");
			}
			Todo todoInfo = todoDtoToEntity(todo);
			todoInfo.setTaskStatus(Todo.TASK_STATUS_TODO);
			todoInfo.setState(Todo.STATE_NORMAL);
			todoRepository.save(todoInfo);
			return todoInfo.getId();
			
		} catch(Exception e) {
			Method nowMethod = new Object(){}.getClass().getEnclosingMethod();
			log.error("["+nowMethod+"] Error.", e);
			throw e;
		}
	}
	
	 public Todo updateTodo(Integer id, TodoDto updatedTodo, Integer updateUserId) throws Exception {
		 if(!updatedTodo.isValidate()) {
			throw new Exception("Bad Request.");
		 }
		 Optional<Todo> optionalTodo = todoRepository.findById(updatedTodo.getId());
		 if (optionalTodo.isPresent()) {
			 
			 Todo todo = optionalTodo.get();
			 
			 // 업데이트할 정보 설정
			 todo.setTodo(updatedTodo.getTodo());
			 todo.setWeight(updatedTodo.getWeight());

			 // 업데이트된 정보 저장
			 todoRepository.save(todo);
			 return todo;
			 
		 } else {
			 // 해당 ID를 가진 Todo를 찾을 수 없는 경우 처리
			 throw new IllegalArgumentException("Todo with id " + id + " not found");
		 }
    }
	 
	public Todo updateTaskStatus(Integer id, Integer taskStatus, Integer updateUserId) throws Exception {
		log.info("[updateTaskStatus] id:{}/taskStatus:{}/updateUserId:{}", id, taskStatus, updateUserId);
		Optional<Todo> optionalTodo = todoRepository.findById(id);
		 if (optionalTodo.isPresent()) {
			 
			 Todo todo = optionalTodo.get();
			 
			 if(todo.getUser().getId() != updateUserId) {
				throw new Exception("Unauthorized.");
			 }
			
			 // todo 상태 => 진행 중, 완료로 변경 가능
			 if(todo.getTaskStatus() == Todo.TASK_STATUS_TODO) {
				 if(taskStatus == Todo.TASK_STATUS_IN_PROGRESS
					|| taskStatus == Todo.TASK_STATUS_FINISH) {
					 todo.setTaskStatus(taskStatus);
				 } else {
					 throw new IllegalArgumentException("Inappropriate status.");
				 }
			 } else if(todo.getTaskStatus() == Todo.TASK_STATUS_IN_PROGRESS) { // 진행 중 상태 => 대기, 완료로 변경 가능
				 if(taskStatus == Todo.TASK_STATUS_PENDING
					|| taskStatus == Todo.TASK_STATUS_FINISH) {
					 todo.setTaskStatus(taskStatus);
				 } else {
					 throw new IllegalArgumentException("Inappropriate status.");
				 }
			 } else if(todo.getTaskStatus() == Todo.TASK_STATUS_PENDING) { // 대기 상태 => 모든 상태로 변경 가능 
				 todo.setTaskStatus(taskStatus);
			 } else if(todo.getTaskStatus() == Todo.TASK_STATUS_FINISH) { // 완료의 경우 변경 불가능
				 throw new IllegalArgumentException("Inappropriate status.");
			 }
			 
		   
			 if(taskStatus == Todo.TASK_STATUS_FINISH) {
				 todo.setFinishedAt(new Date());
			 }
			
			 todoRepository.save(todo);
			 return todo;
			 
		 } else {
			 // 해당 ID를 가진 Todo를 찾을 수 없는 경우 처리
			 throw new IllegalArgumentException("Todo with id " + id + " not found");
		 }

	}
	 
    public void delete(Integer id, Integer userId) {
    	TodoDto todo = getTodo(id);
    	if(todo.getUserId() == userId) {
    		todoRepository.deleteById(id);	
    	} else {
    		throw new IllegalArgumentException("Inappropriate id.");
    	}
    }
	
	public void deleteUserTodoAll(Integer userId) throws Exception {
		try {
			
			log.info("deleteUserTodoAll:" + userId);
			List<Todo> todoList = todoRepository.findByUserId(userId);
			todoRepository.deleteAll(todoList);
			
		} catch(Exception e) {
			Method nowMethod = new Object(){}.getClass().getEnclosingMethod();
			log.error("["+nowMethod+"] Error.", e);
			throw e;
		}
	}

}
