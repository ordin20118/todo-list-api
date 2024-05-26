package com.gwang.todo.common;

import org.springframework.security.core.GrantedAuthority;

import com.gwang.todo.security.CustomUserDetails;

public abstract class BasicController {
	

	/**
	 * 사용자 권한 확인 메소드
	 * @param user		- 확인 대상 사용자 정보
	 * @param roleName	- 확인 권한
	 * @return
	 */
	public boolean hasUserRole(CustomUserDetails user, String roleName) {
		for(Object role : user.getAuthorities()) {
			GrantedAuthority r = (GrantedAuthority)role;
			if(r.getAuthority().equals(roleName)) {
				return true;
			}
		}
		return false;
	}
	  
}
