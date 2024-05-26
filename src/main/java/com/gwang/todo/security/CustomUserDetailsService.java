package com.gwang.todo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gwang.todo.model.user.RefreshToken;
import com.gwang.todo.model.user.User;
import com.gwang.todo.repository.user.RefreshTokenRepository;
import com.gwang.todo.repository.user.UserRepository;

import jakarta.transaction.Transactional;


@Service
public class CustomUserDetailsService implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	RefreshTokenRepository refreshTokenRepository;
	
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Override
	public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmailAndState(username, User.STATE_NORMAL);
		if(user == null) {
			throw new UsernameNotFoundException(username);
		}
		return new CustomUserDetails(user);
	}
	

	@Transactional
	public Token saveJwtToken(CustomUserDetails principal, Token token) {
		
		// 기존 refresh 토큰 제거
		refreshTokenRepository.deleteByUserId(principal.getId());
				
		// 생성된 refresh 토큰 생성
	    User user = new User();
        user.setId(principal.getId());
        
		RefreshToken refreshTkn = new RefreshToken();
		refreshTkn.setRefreshToken(token.getRefreshToken());
		refreshTkn.setExpireDate(token.getRefreshExp());
		refreshTkn.setUser(user);		
		refreshTokenRepository.save(refreshTkn);
		
        return token;
	}

	public PasswordEncoder passwordEncoder() {
        return this.passwordEncoder;
    }
}
