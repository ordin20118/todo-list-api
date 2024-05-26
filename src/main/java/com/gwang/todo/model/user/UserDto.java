package com.gwang.todo.model.user;

import lombok.Data;

@Data
public class UserDto {
	
	public static String emailPattern = "^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$";
	public static String telPattern = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
	
	private Integer id;
	private String 	email;
	private String	password;
	private String	name;
	private String	nickname;
	private String	tel;
	private Integer state;
}
