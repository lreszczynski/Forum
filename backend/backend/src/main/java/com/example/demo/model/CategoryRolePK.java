package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
public class CategoryRolePK implements Serializable {
	@Id
	@Column(name = "category_id")
	private Long categoryId;
	
	@Id
	@Column(name = "role_id")
	private Long roleId;
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CategoryRolePK that = (CategoryRolePK) o;
		return Objects.equals(categoryId, that.categoryId) && Objects.equals(roleId, that.roleId);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(categoryId, roleId);
	}
}
