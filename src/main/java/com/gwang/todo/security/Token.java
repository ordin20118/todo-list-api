package com.gwang.todo.security;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Token {
	
	@JsonProperty("access_token")
	private String 	accessToken;
	
	@JsonProperty("access_exp")
	private Date	accessExp;
	
	@JsonProperty("refresh_token")
	private String 	refreshToken;
	
	@JsonProperty("refresh_exp")
	private Date	refreshExp;	
	
	private Integer 	id;
	private String 		email;
	
	public String getAccessToken() {
		return this.accessToken;
	}
	
	public Date getAccessExp() {
		return this.accessExp;
	}
	
	public String getRefreshToken() {
		return this.refreshToken;
	}
	
	public Date getRefreshExp() {
		return this.refreshExp;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

}
