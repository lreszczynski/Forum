package com.example.demo.role;

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
@Table(name = "app_role", schema = "public", catalog = "demo")
public class Role {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty(access = READ_ONLY)
	private Long id;
	
	@Basic
	@Column(name = "name", unique = true)
	private String name;
	
	@Basic
	@Column(name = "description")
	private String description;
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		Role role = (Role) o;
		return Objects.equals(id, role.id);
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
}
