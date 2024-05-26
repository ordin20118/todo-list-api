package com.gwang.todo.repository.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gwang.todo.model.user.User;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
			
	public User findByName(String name);
	
	public User findByEmail(String email);
	
	public User findByTel(String tel);
	
	public User findByEmailAndState(String email, Integer state);
	
	public Page<User> findAll(Pageable pageable);
	
	public long count();
	
}
