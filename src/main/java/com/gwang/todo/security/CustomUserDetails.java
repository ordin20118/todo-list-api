package com.gwang.todo.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.gwang.todo.model.user.User;
import com.gwang.todo.model.user.UserRole;


public class CustomUserDetails implements UserDetails {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8352830207385543021L;
	private final User user;
	
	public CustomUserDetails(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();

        for (UserRole role : this.user.getRoles()) {
            roles.add(new SimpleGrantedAuthority(role.getRole().getName()));
        }

        return roles;
	}
	
	public Integer getId() {
		return user.getId();
	}
	
	public String getEmail() {
		return user.getEmail();
	}
	
	public String getTel() {
		return user.getTel();
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getName();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
