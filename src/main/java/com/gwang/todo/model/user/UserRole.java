package com.gwang.todo.model.user;


import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity(name="user_role")
public class UserRole {
		
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false)
	private Integer uid;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, name="roleId", referencedColumnName="id")
	private Role role;
	
	public UserRole(){}
	public UserRole(int rid){
		Role role = new Role();
		role.setId(rid);
		this.role = role;
	}
	public UserRole(int uid, int rid){
		this.uid = uid;
		Role role = new Role();
		role.setId(rid);
		this.role = role;
	}

}
