package com.example.demo.category;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

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
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Category category = (Category) o;
		return active == category.active && Objects.equals(id, category.id) && Objects.equals(name, category.name) && Objects.equals(description, category.description);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, name, description, active);
	}
}
