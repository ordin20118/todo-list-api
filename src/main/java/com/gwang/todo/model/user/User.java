package com.gwang.todo.model.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Table(name = "user")
@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@ToString
@DynamicUpdate
@DynamicInsert
public class User {
	
	public static final Integer STATE_NORMAL = 0;
	public static final Integer STATE_WITHDRAWL = 1;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable = false, unique = true, length = 320)
	private String email;
	
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(nullable = false, length = 100)
	private String	password;
	
	@Column(nullable = false, length = 50)
	private String	name;
	
	@Column(nullable = false, length = 50)
	private String	nickname;
	
	@Column(nullable = false, length = 20)
	private String	tel;
	
	private Integer state;
	
	@CreationTimestamp
	@Column(nullable = false)
	private Date	createdAt;
	
	@UpdateTimestamp
	private Date	updatedAt;
	
	@OneToMany(mappedBy="uid", cascade=CascadeType.REMOVE, fetch = FetchType.EAGER)
    //@JoinColumn(name="uid", referencedColumnName="id")
	private List<UserRole> roles;	
	
	public User(){}
	public User(Integer id){
		this.id = id;
	}
	public User(String email, String password, String name, String nickname, String tel) {
		this.email = email;
		this.name = name;
		this.nickname = nickname;
		this.tel = tel;
		this.password = password;
		this.state = STATE_NORMAL;
	}

	
	public List<String> getRolesString() {
		List<String> res = new ArrayList<>();
		for(UserRole role : roles) {
			res.add(role.getRole().getName());
		}
		return res;
	}

}
