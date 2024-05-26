package com.gwang.todo.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.gwang.todo.common.BasicController;
import com.gwang.todo.model.user.Role;
import com.gwang.todo.model.user.User;
import com.gwang.todo.security.CustomUserDetails;
import com.gwang.todo.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ViewController extends BasicController {
	
	@Autowired
	UserService userService;
	
	@Transactional
	@GetMapping(value= {"/", "/page", "/page/index", "/page/home"})
	public String mainPage(Model model, @AuthenticationPrincipal CustomUserDetails user) {	
		if(user == null) 
			return "redirect:login";
		return "index";
	}
	

	@PreAuthorize("hasRole('"+Role.ROLE_USER+"')")
	@GetMapping("/page/videos/input")
	public String inputVideo(Model model) {
		return "input_video";
	}
	
	@GetMapping("/page/registration")
	public String regist(Model model) {
		return "regist";
	}
	
	@GetMapping("/page/login")
	public String login(Model model, @AuthenticationPrincipal CustomUserDetails user) {
		if(user != null) {
			return "redirect:home";
		}
		return "login";
	}
	
	
	@GetMapping("/page/users/modification/{userId}")
	public String modifyUserPage(
			@PathVariable Integer userId
			, @AuthenticationPrincipal CustomUserDetails user
			, Model model) {
		if(user == null) 
			return "redirect:/page/login";
		if(userId != user.getId()) 
			return "redirect:/page/login";
		
		User userInfo = userService.getUser(userId);
		model.addAttribute("user", userInfo);
		return "modify_user";
	}
	
	 
}
