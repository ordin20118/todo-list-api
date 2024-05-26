package com.gwang.todo.model.todo;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import com.gwang.todo.model.user.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Table(name = "todo")
@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@ToString
@DynamicUpdate
@DynamicInsert
public class Todo {
	
	public static final int STATE_NORMAL = 0;
	public static final int STATE_WITHDRAWL = 1;
	
	public static final int TASK_STATUS_TODO = 0;
	public static final int TASK_STATUS_IN_PROGRESS = 1;
	public static final int TASK_STATUS_FINISH = 2;
	public static final int TASK_STATUS_PENDING = 3;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	//private Integer userId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@Column(nullable = false, length = 100)
	private String	todo;
	
	private Integer state;
	
	private Integer taskStatus;
	
	private Integer weight;
	
	@CreationTimestamp
	@Column(nullable = false)
	private Date	createdAt;
	
	@UpdateTimestamp
	private Date	updatedAt;
	private Date	finishedAt;
	
	
	public Todo(){}

}
