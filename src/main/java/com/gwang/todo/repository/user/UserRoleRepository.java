package com.gwang.todo.repository.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gwang.todo.model.user.UserRole;


@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
		
	public UserRole findByRoleName(String name);
	
	public List<UserRole> findByUid(Integer uid);
	
	public Page<UserRole> findAll(Pageable pageable);
	
	public long count();
	
	public long deleteByUid(Integer uid);

}
