package com.gwang.todo.repository.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gwang.todo.model.user.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	
	public Role findById(Integer id);
	
	public Role findByName(String name);
	
	public Page<Role> findAll(Pageable pageable);
	
	public long count();
	

}
