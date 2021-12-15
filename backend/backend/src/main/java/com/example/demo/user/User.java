package com.example.demo.user;

import com.example.demo.role.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.*;

@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "app_user", schema = "public", catalog = "demo")
public class User {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty(access = READ_ONLY)
	private Long id;
	
	@Basic
	@Column(name = "username", unique = true)
	private String username;
	
	@Basic
	@Column(name = "password")
	private String password;
	
	@Basic
	@Column(name = "email", unique = true)
	private String email;
	
	@Basic
	@Column(name = "active")
	private boolean active;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		User user = (User) o;
		return Objects.equals(id, user.id);
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
}
