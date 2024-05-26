package com.gwang.todo.model.user;

import java.util.Date;


import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity(name="refresh_token")
public class RefreshToken {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
		
	@Column(nullable = false, unique = true, length = 300)
	private String	refreshToken;
	
	@Column(nullable = false)
	private Date	expireDate;
	
	@CreationTimestamp
	@Column(nullable = false)
	private Date	regDate;
	
	@OneToOne(targetEntity=User.class, fetch=FetchType.LAZY)
	@JoinColumn(nullable = false, name="user_id", referencedColumnName = "id") 
	private User user;
	
	public RefreshToken() {}
	
}
