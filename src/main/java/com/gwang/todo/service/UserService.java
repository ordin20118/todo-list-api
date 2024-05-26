package com.gwang.todo.service;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gwang.todo.common.Response;
import com.gwang.todo.model.user.Role;
import com.gwang.todo.model.user.User;
import com.gwang.todo.model.user.UserDto;
import com.gwang.todo.model.user.UserRole;
import com.gwang.todo.repository.user.RefreshTokenRepository;
import com.gwang.todo.repository.user.RoleRepository;
import com.gwang.todo.repository.user.UserRepository;
import com.gwang.todo.repository.user.UserRoleRepository;
import com.gwang.todo.security.JwtTokenProvider;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
	
	@PersistenceUnit
	EntityManagerFactory emf;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	UserRoleRepository userRoleRepository;
	
	@Autowired
	RefreshTokenRepository refreshTokenRepository;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@Autowired
	TodoService todoService;
	

	/**
	 * 회원 정보 조회
	 * @param userId
	 * @return
	 * @throws IllegalArgumentException
	 */
	public User getUser(Integer userId) throws IllegalArgumentException {		
		Optional<User> userOp = userRepository.findById(userId);
		User user = userOp.get();		
		if(user == null) {
			throw new IllegalArgumentException("Not Found User");
		}		
        return user;
	}
	
	public Page<User> getUserList(Pageable pageable) {
		return userRepository.findAll(pageable);
	}
	
	/**
	 * 회원 가입
	 * @param userParam	- 가입 요청 정보
	 * @return User	- 가입된 사용자 정보
	 * @throws Exception
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={Exception.class})
	public Response registUser(UserDto userParam) throws Exception {
		
		Response res = Response.successInstance();
		res.setHttpStatus(HttpStatus.CREATED);
		
		try {
			
			User user = new User();
			
			// Validate Data			
			// 이름
			if(userParam.getName() != null && userParam.getName().length() > 0) {				
				user.setName(userParam.getName());
			} else {
				res = res.fail();
				res.setMessage("No Validated Name.");
				return res;
			}
			
			// 닉네임
			if(userParam.getNickname() != null && userParam.getNickname().length() > 0) {				
				user.setNickname(userParam.getNickname());
			} else {
				res = res.fail();
				res.setMessage("No Validated Nickname.");
				return res;
			}
			
			// 이메일
			if(userParam.getEmail() != null && userParam.getEmail().length() > 0
				&& Pattern.matches(UserDto.emailPattern, userParam.getEmail())) {
				user.setEmail(userParam.getEmail());
			} else {
				res = res.fail();
				res.setMessage("No Validated Email.");
				return res;
			}
			
			// 전화번호
			if(userParam.getTel() != null && userParam.getTel().length() > 0
				&& Pattern.matches(UserDto.telPattern, userParam.getTel())) {
				user.setTel(userParam.getTel());	
			} else {
				res = res.fail();
				res.setMessage("No Validated Tel.");
				return res;
			}
			
			User emailUser = userRepository.findByEmail(userParam.getEmail());
			if(emailUser != null) {
				res = res.fail();
				res.setMessage("Duplicated Email.");
				return res;
			}
			
			user.setPassword(passwordEncoder.encode(userParam.getPassword()));
			userRepository.save(user);
			
			log.info("registered user");

			Role role = roleRepository.findByName("ROLE_USER");
			UserRole uRole = new UserRole(user.getId(), role.getId());
			userRoleRepository.save(uRole);
			
			return res;
			
		} catch(Exception e) {
			Method nowMethod = new Object(){}.getClass().getEnclosingMethod();
			log.error("["+nowMethod+"] Error.", e);
			throw e;
		}
	}
	
	/**
	 * 회원 탈퇴
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={Exception.class})
	public void withdrawUser(Integer userId) throws Exception {
		
		try {
									
			// 사용자 DB 처리
			User userData = userRepository.findById(userId).get();
			userData.setState(User.STATE_WITHDRAWL);

			// 사용자의 컨텐츠 삭제 처리
			todoService.deleteUserTodoAll(userId);
			
			// refresh token 제거			
			refreshTokenRepository.deleteByUserId(userId);
			
			// 사용자 데이터 제거
			// TODO: update state 
			userRepository.save(userData);
						
		} catch(Exception e) {
			Method nowMethod = new Object(){}.getClass().getEnclosingMethod();
			log.error("["+nowMethod+"] Error.", e);
			throw e;
		}
	}
	

	/**
	 * 회원 정보 수정
	 * @param userId		- 수정 대상 사용자 ID
	 * @param userParam		- 수정된 사용자 정보
	 * @return
	 * @throws Exception
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={Exception.class})
	public String modifyUser(Integer userId, UserDto userParam) throws Exception {
		
		try {
			
			log.info("[User Modify]:"+userParam);
			
			Optional<User> userOp = userRepository.findById(userId);
			User user = userOp.get();
			
			if(userParam.getPassword() != null && userParam.getPassword().length() > 0) {
				user.setPassword(passwordEncoder.encode(userParam.getPassword()));	
			}
			
			if(userParam.getName() != null && userParam.getName().length() > 0) {
				user.setName(userParam.getName());	
			}
			
			if(userParam.getNickname() != null && userParam.getNickname().length() > 0) {
				user.setNickname(userParam.getNickname());	
			}
			
			if(userParam.getTel() != null && userParam.getTel().length() > 0
				&& Pattern.matches(UserDto.telPattern, userParam.getTel())) {
				user.setTel(userParam.getTel());	
			} else {
				throw new Exception("No Validated Tel.");
			}			
			
			userRepository.save(user);
			
		} catch(Exception e) {
			Method nowMethod = new Object(){}.getClass().getEnclosingMethod();
			log.error("["+nowMethod+"] Error.", e);
			throw e;
		}
		
		return "success";
	}
	
}
