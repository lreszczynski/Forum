package com.example.demo.categoryroles;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "app_category_role", schema = "public")
@IdClass(CategoryRolePK.class)
public class CategoryRole {
	@Id
	@Column(name = "category_id")
	private Long categoryId;
	
	@Id
	@Column(name = "role_id")
	private Long roleId;
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		CategoryRole that = (CategoryRole) o;
		return categoryId != null && Objects.equals(categoryId, that.categoryId)
				&& roleId != null && Objects.equals(roleId, that.roleId);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(categoryId, roleId);
	}
}
