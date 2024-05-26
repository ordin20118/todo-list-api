package com.gwang.todo.security;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.server.MethodNotAllowedException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwang.todo.model.user.UserDto;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;
	private JwtTokenProvider jwtTokenProvider;
	private CustomUserDetailsService userService;
	private ObjectMapper mapper;
	
	public CustomLoginFilter(AuthenticationManager authenticationManager, JwtTokenProvider tokenManager, CustomUserDetailsService userService, ObjectMapper mapper) {
	    this.authenticationManager = authenticationManager;
	    this.jwtTokenProvider = tokenManager;
	    this.userService = userService;
	    this.mapper = mapper;
	    super.setFilterProcessesUrl(SecurityConstant.API_URL_LOGIN);
	}
	
	  
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		if (!request.getMethod().equals(HttpMethod.POST.name())) {
		      throw new MethodNotAllowedException(request.getMethod(), Arrays.asList(HttpMethod.POST));
		}
		
		try (InputStream is = request.getInputStream()) {
			
			UserDto loginParam = new ObjectMapper().readValue(is, UserDto.class);
		    return authenticationManager.authenticate(
		          new UsernamePasswordAuthenticationToken(
		        		  loginParam.getEmail(),
		        		  loginParam.getPassword(),
		        		  Collections.emptyList())
		    );
		} catch (IOException e) {
		      throw new RuntimeException(e);
		}		
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		
		CustomUserDetails principal = (CustomUserDetails) authResult.getPrincipal();
		
		Token token = jwtTokenProvider.createToken(principal.getEmail(), principal.getAuthorities());
		token.setId(principal.getId());
		token.setEmail(principal.getEmail());
		
		userService.saveJwtToken(principal, token);
				
		response.setContentType(APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(mapper.writeValueAsString(token));
		response.getWriter().flush();
		
	}
	
}

