package com.gwang.todo.controller.api;

import java.lang.reflect.Method;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwang.todo.common.BasicController;
import com.gwang.todo.common.Response;
import com.gwang.todo.model.user.RefreshToken;
import com.gwang.todo.model.user.User;
import com.gwang.todo.model.user.UserDto;
import com.gwang.todo.repository.user.RefreshTokenRepository;
import com.gwang.todo.security.CustomUserDetails;
import com.gwang.todo.security.CustomUserDetailsService;
import com.gwang.todo.security.JwtTokenProvider;
import com.gwang.todo.security.Token;
import com.gwang.todo.service.UserService;
import com.gwang.todo.util.CookieUtil;
import com.gwang.todo.util.ObjectMapperInstance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "회원 API", description = "회원 정보에 대한 처리")
@Slf4j
@RestController
@RequestMapping("/api")
public class UserApiController extends BasicController {

	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@Autowired
	UserService userService;
	
	@Autowired
	CustomUserDetailsService userDetailsService;
	
	@Autowired
	RefreshTokenRepository refreshTokenRepository;
	
		
	/**
	 * Access Token 재발급 API
	 * @param token
	 * @param response
	 * @return
	 */
	@Operation(
		summary = "Access 토큰 재발급", 
		description = "Access 토큰이 만료된 경우, Refresh 토큰을 이용하여 Access 토큰을 재발급 받습니다."
	)
	@Transactional
	@PostMapping(value="/auth/token")	
	public ResponseEntity<String> issueAccessToken(@RequestBody Token token, HttpServletResponse response) {
		
		String resString = "{}";
		HttpStatus resStatus = HttpStatus.OK;	
		ObjectMapper mapper = ObjectMapperInstance.getInstance().getMapper();
				 
		try {
							
			if(token.getRefreshToken() == null || token.getRefreshToken().length() == 0) {
				return ResponseEntity.badRequest().build();
			}
			
			boolean validateExp = jwtTokenProvider.validateExp(token.getRefreshToken());
			if(!validateExp) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}
						
			// refresh 토큰으로 DB 조회 
			Optional<RefreshToken> tokenNullable = refreshTokenRepository.findByRefreshToken(token.getRefreshToken());
			RefreshToken tokenData = null;
			if(!tokenNullable.isPresent()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			} else {
				tokenData = tokenNullable.get();
			}
			
			User findUser = userService.getUser(tokenData.getUser().getId());
			
			// access 토큰 신규 발급
			CustomUserDetails user = userDetailsService.loadUserByUsername(findUser.getEmail());
			
			Token newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getAuthorities());	        
	        Token newToken = Token.builder().accessToken(newAccessToken.getAccessToken()).accessExp(newAccessToken.getAccessExp()).build();	        
	        
	        // refresh_token의 만료 기간이 1일 이하라면 갱신
	        long reaminTime = jwtTokenProvider.getRemainTime(token.getRefreshToken());
	        if(reaminTime <= 24 * 60 * 60 * 1000) {
	        	
	        	Token newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());	        	
	        	newToken.setRefreshToken(newRefreshToken.getRefreshToken());
	        	newToken.setId(user.getId());
	        	newToken.setEmail(user.getEmail());
	        	
	        	RefreshToken newRefresh = new RefreshToken();
	        	newRefresh.setRefreshToken(newRefreshToken.getRefreshToken());
	        	newRefresh.setExpireDate(newRefreshToken.getRefreshExp());
	        	newRefresh.setUser(findUser);
	        	refreshTokenRepository.save(newRefresh);								// 새로운 refresh 토큰 저장
	        	
	        	refreshTokenRepository.deleteByRefreshToken(token.getRefreshToken());	// 기존 refresh 토큰 제거
	        }
	        
	        return ResponseEntity.ok(mapper.writeValueAsString(newToken));
	        
		} catch(Exception e) {
			resStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			log.error("[Auth Refresh Token Error]:"+e.getMessage(), e);
		}
		
		return new ResponseEntity<String>(resString, resStatus);
	}
	
	
	/**
	 * 회원 가입 API	 
	 * @param userParam
	 * @return
	 */
	@Operation(
			summary = "회원가입", 
			description = "이메일, 패스워드, 이름, 닉네임 모두 필수 입력사항 입니다."
	)
	@PostMapping(value="/user")
	public Response registration(@RequestBody UserDto userParam) {
		Response res = Response.successInstance();
		try {
			res = userService.registUser(userParam);
		} catch(Exception e) {
			Method nowMethod = new Object(){}.getClass().getEnclosingMethod();
			log.error("["+nowMethod+"] Error.", e);
			res = res.fail();
			res.setMessage("Internal server error.");
			return res;
		}
		return res;
	}
	
	/**
	 * 회원 탈퇴 API
	 * @param response
	 * @param user
	 * @return
	 */
	@Operation(
			summary = "회원 탈퇴", 
			description = ""
	)
	@DeleteMapping(value="/user/{userId}")
	public Response withdraw(
			@PathVariable Integer userId, 
			@AuthenticationPrincipal CustomUserDetails user,
			HttpServletResponse response) {
		
		Response res = new Response();
		
		try {
			
			log.info("withdraw:" + user.getId());
			
			if(user.getId() != userId) {
				return res.fail();
			}
			
			userService.withdrawUser(user.getId());
			
			// 쿠키 제거
			response.addCookie(CookieUtil.makeCookie("access_token", null, 0));
			response.addCookie(CookieUtil.makeCookie("refresh_token", null, 0));
			
			res = res.success();
			res.setMessage("User deleted successfully");
			
		} catch(Exception e) {
			Method nowMethod = new Object(){}.getClass().getEnclosingMethod();
			log.error("["+nowMethod+"] Error.", e);
			return res = res.fail();
		}
		
		return res;
	}
	
	/**
	 * 사용자 정보 수정 API
	 * @param userId
	 * @param userParam
	 * @param user
	 * @return
	 */
	@Operation(
			summary = "회원 정보 수정", 
			description = "비밀번호, 이름, 닉네임, 전화번호에 대한 정보를 수정합니다."
	)
	@PutMapping(value="/user/{userId}")
	public Response modifyUser(
			@PathVariable Integer userId, 
			@RequestBody UserDto userParam, 
			@AuthenticationPrincipal CustomUserDetails user) {
			
		Response res = Response.successInstance();
		
		log.info("[modifyUser]"+userParam);
		
		try {
			
			if(userId == user.getId()) {				
				userService.modifyUser(userId, userParam);	
				res.setMessage("Update user successfully.");
			} else {
				res = res.fail();
				res.setHttpStatus(HttpStatus.UNAUTHORIZED);
			}
			
		} catch(Exception e) {
			Method nowMethod = new Object(){}.getClass().getEnclosingMethod();
			log.error("["+nowMethod+"] Error.", e);
			res = res.fail();
			return res;		
		}
		
		return res;
	}
	
	/**
	 * 사용자 정보 조회 API
	 * @param user - 헤더의 Authorization의 Bearer 인증된 사용자 정보
	 * @return
	 */
	@Operation(
			summary = "회원 정보 조회", 
			description = ""
	)
	@GetMapping(value="/user")
	public Response getUser(@AuthenticationPrincipal CustomUserDetails user){
		Response res = null;
		
		try {
			User userData = userService.getUser(user.getId());				
			
			res = Response.successInstance();
			res.setData(userData);
			return res;
			
		} catch(Exception e) {
			Method nowMethod = new Object(){}.getClass().getEnclosingMethod();
			log.error("["+nowMethod+"] Error.", e);
			res = res.fail();
			return res;	
		}
	}
	 
}
