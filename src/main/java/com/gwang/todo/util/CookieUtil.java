package com.gwang.todo.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {
	
	public static Cookie makeCookie(String name, String val, int expTime) {
		 Cookie cookie = new Cookie(name, val);         
         cookie.setMaxAge(expTime);
         cookie.setHttpOnly(true);
         cookie.setPath("/");
         return cookie;
	}
	
	public static String getCookieValue(Cookie[] cookies, String findName) {
		if(cookies != null && cookies.length > 0) {
			for (Cookie c : cookies) {
	             String name = c.getName(); // 쿠키 이름 가져오기
	             String value = c.getValue(); // 쿠키 값 가져오기
	             if (name.equals(findName)) {                    	
	             	return value;
	             }
	         }	
		}		 
		return null;
	}

}
