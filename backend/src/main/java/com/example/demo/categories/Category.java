package com.example.demo.categories;

import com.example.demo.roles.Role;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "app_category", schema = "public", catalog = "demo")
public class Category {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@JsonProperty(access = READ_ONLY)
	private Long id;
	
	@Basic
	@Column(name = "name", unique = true)
	private String name;
	
	@Basic
	@Column(name = "description")
	private String description;
	
	@Basic
	@Column(name = "active")
	private boolean active;
	
	@ToString.Exclude
	@ManyToMany
	@JoinTable(name = "app_category_role",
			joinColumns = @JoinColumn(name = "category_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new LinkedHashSet<>();
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		Category category = (Category) o;
		return id != null && Objects.equals(id, category.id);
	}
	
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
