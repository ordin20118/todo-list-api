package com.gwang.todo.controller.api;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gwang.todo.common.BasicController;
import com.gwang.todo.common.Response;
import com.gwang.todo.model.todo.TodoDto;
import com.gwang.todo.security.CustomUserDetails;
import com.gwang.todo.service.TodoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "ToDo API", description = "ToDo 정보에 대한 처리")
@Slf4j
@RestController
@RequestMapping("/api")
public class TodoApiController extends BasicController {
		
	@Autowired
	private TodoService todoService;
	
	/**
	 * 할 일 생성 API	 
	 * @return
	 */
	@Operation(
		summary = "할 일 생성", 
		description = "할 일에 대한 내용과 함께 우선순위 정도를 등록합니다."
	)
	@PostMapping(value="/todo")
	public ResponseEntity<String> saveTodo(
			@RequestBody TodoDto todo
			, @AuthenticationPrincipal CustomUserDetails user) {
		
		try {
			
			todo.setUserId(user.getId());
			todoService.saveTodo(todo);
			
		} catch(Exception e) {
			log.error("[Registration Error]:"+e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	/**
	 * 최근 등록 todo 조회 
	 * @return
	 */
	@Operation(
		summary = "최근 등록한 항목 조회", 
		description = "최근 등록한 할 일 정보를 조회합니다."
	)
	@GetMapping(value="/todo/latest")
	public Response getLatest(@AuthenticationPrincipal CustomUserDetails user) {
			
		Response res = new Response();
		
		try {
			
			TodoDto latestTodo = todoService.getLatestTodo(user.getId());
			res = res.success();
			res.setData(latestTodo);
			
		} catch(Exception e) {
			Method nowMethod = new Object(){}.getClass().getEnclosingMethod();
			log.error("["+nowMethod+"] Error.", e);
			throw e;
		}
		
		return res;
	}
	
	/**
	 * Todo 리스트 조회 
	 * @param taskStatus
	 * @param pageable
	 * @param user
	 * @return
	 */
	@Operation(
		summary = "할 일 목록 조회", 
		description = "할 일 목록을 조회합니다.\n상태 값에 대한 필터와 정렬 방식을 지정할 수 있습니다."
	)
	@GetMapping("/todo/list")
    public Response list(
    		//@ModelAttribute PageRequestDto pageRequest
            @RequestParam(required = false) Integer taskStatus
            , Pageable pageable
    		, @AuthenticationPrincipal CustomUserDetails user){

		Response res = Response.successInstance();
		 
		try {
			
			res.setData(todoService.getList(taskStatus, user.getId(), pageable));
			
		} catch(Exception e) {
			Method nowMethod = new Object(){}.getClass().getEnclosingMethod();
			log.error("["+nowMethod+"] Error.", e);
			res = res.fail();
			return res;	
		}
		
		return res;
    }
	
	/**
	 * Todo 정보 수정 API
	 * @return
	 */
	@Operation(
		summary = "할 일 수정", 
		description = "할 일에 대한 내용과 함께 우선순위 정도를 수정합니다.\n상태값은 수정되지 않습니다."
	)
	@PutMapping(value="/todo/{id}")
	public Response updateTodo(
			@PathVariable Integer id
			, @RequestBody TodoDto todoInfo
			, @AuthenticationPrincipal CustomUserDetails user) {
			
		Response res = Response.successInstance();
		 
		try {
			log.info("Delete todo:" + id);
			
			todoService.updateTodo(id, todoInfo, user.getId());
			res.setMessage("Update Todo successfully");
			
		} catch(Exception e) {
			Method nowMethod = new Object(){}.getClass().getEnclosingMethod();
			log.error("["+nowMethod+"] Error.", e);
			res = res.fail();
			return res;	
		}
		
		return res;
	}
	

	/**
	 * Todo 상태 수정 API
	 * @return
	 */
	@Operation(
		summary = "할 일의 상태를 변경합니다.", 
		description = "할 일, 진행 중, 완료, 대기\n진행 중 상태에서만 대기 상태로 변경될 수 있습니다."
					+ "\n대기 상태에서는 어떤 상태로든 변경할 수 있습니다."
	)
	@PatchMapping(value="/todo/{id}")
	public Response updateTodoStatus(
			@PathVariable Integer id
			, @RequestBody TodoDto todoInfo
			, @AuthenticationPrincipal CustomUserDetails user) {
			
		Response res = Response.successInstance();
		 
		try {
			log.info("Update todo status:" + id);
			todoService.updateTaskStatus(id, todoInfo.getTaskStatus(), user.getId());
			res.setMessage("Update Todo status successfully");
			
		} catch(Exception e) {
			Method nowMethod = new Object(){}.getClass().getEnclosingMethod();
			log.error("["+nowMethod+"] Error.", e);
			res = res.fail();
			return res;	
		}
		
		return res;
	}
	
	@Operation(
			summary = "할 일 제거", 
			description = ""
		)
	@DeleteMapping("/todo/{id}")
    public Response delete(
    		@PathVariable Integer id
			, @AuthenticationPrincipal CustomUserDetails user) {

		Response res = Response.successInstance();
		try {
			todoService.delete(id, user.getId());
			res.setMessage("Delete Todo successfully");
			
		} catch(Exception e) {
			Method nowMethod = new Object(){}.getClass().getEnclosingMethod();
			log.error("["+nowMethod+"] Error.", e);
			res = res.fail();
			return res;	
		}
        return res;
    }
	 
}
