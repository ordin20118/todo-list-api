package com.gwang.todo.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gwang.todo.model.user.RefreshToken;


@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
		
	Optional<RefreshToken> findByRefreshToken(String refreshToken);
	Optional<RefreshToken> deleteByUserId(Integer userId);
	Optional<RefreshToken> deleteByRefreshToken(String refreshToken);
	
}
