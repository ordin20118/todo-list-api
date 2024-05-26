package com.gwang.todo.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.gwang.todo.repository.user.RefreshTokenRepository;
import com.gwang.todo.util.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

@Component
public class CustomLogoutHandler implements LogoutHandler {

	@Autowired
	RefreshTokenRepository refreshTokenRepository;
	
	@Transactional
	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

		if(authentication == null) {
			try {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			} catch (IOException e) {}
		} else {

			// DB 토큰 제거
			CustomUserDetails user = (CustomUserDetails)authentication.getPrincipal();			
			refreshTokenRepository.deleteByUserId(user.getId());
			
			// 쿠키 제거
			response.addCookie(CookieUtil.makeCookie(SecurityConstant.COOKIE_ACCESS_TOKEN_NAME, null, 0));
			response.addCookie(CookieUtil.makeCookie(SecurityConstant.COOKIE_REFRESH_TOKEN_NAME, null, 0));		
		}
		
	}

}
