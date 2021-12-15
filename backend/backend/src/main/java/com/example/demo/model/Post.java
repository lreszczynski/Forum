package com.example.demo.model;

import com.example.demo.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "app_post", schema = "public", catalog = "demo")
public class Post {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty(access = READ_ONLY)
	private Long id;
	
	@Basic
	@Column(name = "content")
	private String content;
	
	@Basic
	@Column(name = "create_date")
	private Timestamp createDate;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "thread_id", nullable = false)
	private Thread thread;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		Post post = (Post) o;
		return Objects.equals(id, post.id);
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
}
