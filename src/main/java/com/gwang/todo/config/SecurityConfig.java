package com.gwang.todo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwang.todo.security.CustomLoginFilter;
import com.gwang.todo.security.CustomLogoutHandler;
import com.gwang.todo.security.CustomUserDetailsService;
import com.gwang.todo.security.JwtAccessDeniedHandler;
import com.gwang.todo.security.JwtAuthenticationEntryPoint;
import com.gwang.todo.security.JwtAuthenticationFilter;
import com.gwang.todo.security.JwtTokenProvider;
import com.gwang.todo.security.SecurityConstant;
import com.gwang.todo.security.WebJwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@DependsOn("jwtTokenProvider")
public class SecurityConfig extends WebSecurityConfiguration {
		
	@Autowired
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	
	@Autowired
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
	
	@Autowired
    private final CustomLogoutHandler logoutHandler;
	
    @Autowired
    private final CustomUserDetailsService userService;
    
    @Autowired
    private final JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private final ObjectMapper mapper;
    
    
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
            .requestMatchers("/fonts/**", "/img/**", "/js/**", "/css/**");
    }
    
	@Bean
	@DependsOn("jwtTokenProvider")
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		
		AuthenticationManagerBuilder sharedObject = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
        AuthenticationManager authenticationManager = sharedObject.build();
        httpSecurity.authenticationManager(authenticationManager);
		
		httpSecurity
		.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
        	authorizationManagerRequestMatcherRegistry
	        	//.requestMatchers("/page/login", "/page/registration").permitAll()
        		.requestMatchers("/page/**").permitAll()
				.requestMatchers(HttpMethod.POST, SecurityConstant.API_URL_LOGIN, SecurityConstant.API_URL_TOKEN, SecurityConstant.API_URL_USER).permitAll()
                .requestMatchers("/admin/**").hasAnyRole("ADMIN")
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/users/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/swagger-ui**").permitAll()
                .anyRequest().authenticated())
				.httpBasic(HttpBasicConfigurer::disable)
				// REST API이므로 basic auth 및 csrf 보안을 사용하지 않음
				.csrf(AbstractHttpConfigurer::disable)
				.addFilter(new CustomLoginFilter(authenticationManager, jwtTokenProvider, userService, mapper))
				.formLogin(AbstractHttpConfigurer::disable)
				
				// JWT를 사용하기 때문에 세션을 사용하지 않음
				.sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        httpSecurity.exceptionHandling()
        			.authenticationEntryPoint(jwtAuthenticationEntryPoint)
        			.accessDeniedHandler(jwtAccessDeniedHandler);

        // JWT 인증을 위하여 직접 구현한 필터를 UsernamePasswordAuthenticationFilter 전에 실행
        httpSecurity.addFilterBefore(new JwtAuthenticationFilter(userService, jwtTokenProvider), LogoutFilter.class);
		httpSecurity.addFilterBefore(new WebJwtAuthenticationFilter(userService, jwtTokenProvider), JwtAuthenticationFilter.class);
		httpSecurity.logout(logout -> logout
            .logoutUrl(SecurityConstant.API_URL_LOGOUT)
            .addLogoutHandler(logoutHandler)
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
        );
		
		return httpSecurity.build();
	}

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt Encoder 사용
    	return new BCryptPasswordEncoder();
    }
     
}
