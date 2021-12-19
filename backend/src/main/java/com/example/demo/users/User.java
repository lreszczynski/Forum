package com.example.demo.users;

import com.example.demo.roles.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

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
	
	@Column(name = "username", unique = true)
	private String username;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "email", unique = true)
	private String email;
	
	@Column(name = "active")
	private boolean active;
	
	@Column(name = "banned", nullable = false)
	private boolean banned = false;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		User user = (User) o;
		return id != null && Objects.equals(id, user.id);
	}
	
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
