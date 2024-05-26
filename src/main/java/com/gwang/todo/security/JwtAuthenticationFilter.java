package com.gwang.todo.security;

import java.io.IOException;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	 
	private CustomUserDetailsService userDetailsService;
	private JwtTokenProvider jwtTokenProvider;

	
	public JwtAuthenticationFilter(CustomUserDetailsService userService, JwtTokenProvider jwtTokenProvider) {
		this.userDetailsService = userService;
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		final String requestTokenHeader = request.getHeader("Authorization");
		
        String username = null;
        String jwtToken = null;

        // 헤더의 "Authorization" 값에서 "Bearer"를 제외한 JWT 토큰을 추출        
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
        	
        	jwtToken = requestTokenHeader.substring(7);
                        
            try {
                username = jwtTokenProvider.getUserName(jwtToken);
            } catch (IllegalArgumentException e) {
            	log.error("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
            	log.error("JWT Token has expired");            	
            }
            
        } else {            
        	//log.error("JWT Token does not begin with Bearer String");
        }

        
        // 토큰 유효성 검사 
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            CustomUserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            
            // 토큰이 유효한 경우 수동으로 인증 정보 설정          
			if (jwtTokenProvider.validateToken(jwtToken, userDetails)) {
			    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
			            userDetails, null, userDetails.getAuthorities());
			    usernamePasswordAuthenticationToken
			            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);                
			}
        }
        
        filterChain.doFilter(request, response);		
	}

}
