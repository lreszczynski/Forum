package com.example.demo.threads;

import com.example.demo.categories.Category;
import com.example.demo.posts.Post;
import com.example.demo.users.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "app_thread", schema = "public", catalog = "demo")
public class Thread {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty(access = READ_ONLY)
	private Long id;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "active")
	private boolean active;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@OneToMany(mappedBy = "thread", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private Set<Post> posts = new LinkedHashSet<>();
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		Thread thread = (Thread) o;
		return id != null && Objects.equals(id, thread.id);
	}
	
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
