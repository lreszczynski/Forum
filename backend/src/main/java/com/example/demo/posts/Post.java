package com.example.demo.posts;

import com.example.demo.threads.Thread;
import com.example.demo.users.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "app_post", schema = "public")
public class Post {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty(access = READ_ONLY)
	private Long id;
	
	@Column(name = "content")
	private String content;
	
	@Column(name = "create_date")
	private Instant createDate;
	
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
		return id != null && Objects.equals(id, post.id);
	}
	
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
