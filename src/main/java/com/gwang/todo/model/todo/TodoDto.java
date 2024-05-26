package com.gwang.todo.model.todo;

import java.util.Date;

import com.gwang.todo.model.user.User;
import com.gwang.todo.model.user.UserDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class TodoDto {
	
	private Integer id;
	
	private Integer userId;
	
	@Schema(description = "할 일 내용")
	private String	todo;
	
	@Schema(description = "할 일 데이터 상태(0: 정상, 1: 제거)")
	private Integer state;
	
	@Schema(description = "할 일 진행 상태(0: 할 일 , 1: 진행 중, 2: 완료, 3: 대기")
	private Integer taskStatus;
	
	@Schema(description = "우선순위(20:낮음 30:보통, 50:높음 80:매우 높음)")
	private Integer weight;
	
	private Date	createdAt;
	
	private Date	updatedAt;
	
	@Schema(description = "완료 날짜")
	private Date	finishedAt;
	
	private UserDto user;
	
	public boolean isValidate() {
		if(this.todo == null || this.todo.length() <= 0) {
			return false;
		}
		if(this.weight == null) {
			return false;
		}
		return true;
	}
	
}
