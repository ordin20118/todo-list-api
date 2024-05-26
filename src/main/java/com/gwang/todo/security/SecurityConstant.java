package com.gwang.todo.security;

public class SecurityConstant {
	
	// auth
	public static final String API_URL_REGIST 	= "/api/user";
	public static final String API_URL_LOGIN 	= "/api/auth/login";
	public static final String API_URL_TOKEN 	= "/api/auth/token";
	public static final String API_URL_LOGOUT 	= "/api/auth/logout";	
	public static final String API_URL_USER		= "/api/user";
	
	// page
	public static final String PAGE_URL_REGIST 		= "/page/registration";
	public static final String PAGE_URL_LOGIN 		= "/page/login";	
	public static final String PAGE_URL_USER_MODIFY = "/page/user/modification";
		
	// cookie name
	public static final String COOKIE_ACCESS_TOKEN_NAME  = "access_token";
	public static final String COOKIE_REFRESH_TOKEN_NAME = "refresh_token";
	
}
