package com.gwang.todo.security;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("jwtTokenProvider")
@RequiredArgsConstructor
public class JwtTokenProvider {
	
	String secretKeyString = "YIHLJfk0e+uEWNMAtwYEOOzxfVnkJAfs01PtuMaFkwc=";
	SecretKey key = null;
	

    public static final long JWT_ACCESS_TOKEN_VALID_TIME = 30 * 60 * 1000L;			// 30min
    public static final long JWT_REFRESH_TOKEN_VALID_TIME = 7 * 24 * 60 * 60 * 1000L;	// 7day
    //public static final long JWT_REFRESH_TOKEN_VALID_TIME = 23 * 60 * 60 * 1000L;
    
    private final CustomUserDetailsService userDetailsService;
    
    @PostConstruct
    protected void init() {
    	byte[] secretKeyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        key = new SecretKeySpec(secretKeyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    // JWT access 토큰 생성
    public Token createAccessToken(String username, Collection<? extends GrantedAuthority> roles) {
        Claims claims = Jwts.claims().setSubject(username); // email 저장
        claims.put("roles", roles); // 정보는 key / value 쌍으로 저장된다.
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + JWT_ACCESS_TOKEN_VALID_TIME);
        String accessToken = Jwts.builder()
				        		.setIssuer("todo")
				                .setClaims(claims) // 정보 저장
				                .setIssuedAt(now) // 토큰 발행 시간 정보
				                .setExpiration(expireDate) // set Expire Time
				                .signWith(key)
				                .compact();  
        return Token.builder().accessToken(accessToken).accessExp(expireDate).build();         
    }
    
    // JWT refresh 토큰 생성
    public Token createRefreshToken(String username) {
    	 Date now = new Date();
    	 Date expireDate = new Date(now.getTime() + JWT_REFRESH_TOKEN_VALID_TIME);
         String accessToken = Jwts.builder()
 				        		.setIssuer("todo")
 				                .setIssuedAt(now) // 토큰 발행 시간 정보
 				                .setExpiration(expireDate) // set Expire Time
 				                .signWith(key)
 				                .compact();  
         return Token.builder().refreshToken(accessToken).refreshExp(expireDate).build();         
    }
    
    // JWT 토큰 생성
    public Token createToken(String username, Collection<? extends GrantedAuthority> roles) {
    	Token access = createAccessToken(username, roles);
    	Token refresh = createRefreshToken(username);
    	return Token.builder()
    				.accessToken(access.getAccessToken())
    				.refreshToken(refresh.getRefreshToken())
    				.accessExp(access.getAccessExp())
    				.refreshExp(refresh.getRefreshExp())
    				.build();
    }

    // JWT 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        CustomUserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserName(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰에서 회원 정보 추출
    public String getUserName(String token) {    	
    	return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }
    
    // 만료까지 남은 시간 반환
    public long getRemainTime(String token) {
    	Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    	Date expDate = claims.getBody().getExpiration();
    	return Math.abs(expDate.getTime() - new Date().getTime());
    }
    
    public boolean validateExp(String jwtToken) {
    	Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtToken);
    	return !claims.getBody().getExpiration().before(new Date());      
    }

    // 토큰의 유효성 + 만료일자 확인
    public boolean validateToken(String jwtToken, CustomUserDetails userDetails) {        
    	try {        	        	
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtToken);
            boolean isSameUser = claims.getBody().getSubject().equals(userDetails.getEmail());
            boolean isExpired = claims.getBody().getExpiration().before(new Date());                        
            return isSameUser && !isExpired;
        } catch (Exception e) {
            return false;
        }
    }

}
