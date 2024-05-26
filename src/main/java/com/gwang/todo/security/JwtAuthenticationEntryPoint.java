package com.gwang.todo.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
	
	@Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {		
		String requestUri = request.getRequestURI();
		if(requestUri.indexOf("page") > 0 || requestUri.equals("/") 
				|| requestUri.indexOf("swagger") > 0) {
			response.sendRedirect("/page/login");
		} else {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");	
		}
    }
}
