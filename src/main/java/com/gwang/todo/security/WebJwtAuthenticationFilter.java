package com.gwang.todo.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.gwang.todo.util.CookieUtil;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebJwtAuthenticationFilter extends OncePerRequestFilter {
	 
	private CustomUserDetailsService userDetailsService;
	private JwtTokenProvider jwtTokenProvider;

	
	public WebJwtAuthenticationFilter(CustomUserDetailsService userService, JwtTokenProvider jwtTokenProvider) {
		this.userDetailsService = userService;
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

        String username = null;
        String accessToken = null;
        String refreshToken = null;
                
        String uri = request.getRequestURI();
        if(uri.startsWith("/page") || uri.equals("/") || uri.equals("/index")) {
        }
    	 Cookie[] cookies = request.getCookies(); // 모든 쿠키 가져오기
         if(cookies != null) {
         	
         	accessToken = CookieUtil.getCookieValue(cookies, "access_token");

         	try {
         		if(accessToken != null) {
         			username = jwtTokenProvider.getUserName(accessToken);	
         		}                
             } catch (IllegalArgumentException e) {
             	log.error("Unable to get JWT Token");
             } catch (ExpiredJwtException e) {
             	log.error("JWT Token has expired");
             } catch (Exception e) {}
         }

         // 토큰 유효성 검사 
         if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
         	
             CustomUserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

             // 토큰이 유효한 경우 수동으로 인증 정보 설정          
 			if (jwtTokenProvider.validateToken(accessToken, userDetails)) {
 			    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
 			            userDetails, null, userDetails.getAuthorities());
 			    usernamePasswordAuthenticationToken
 			            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

 			    // 컨텍스트에서 인증 정보 설정
 			    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);                
 			}
         }
        
        filterChain.doFilter(request, response);		
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		// TODO Auto-generated method stub
		return super.shouldNotFilter(request);
	}

}
