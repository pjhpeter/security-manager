package org.my.heart.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 角色和菜单关联表
 * 
 * @author 彭嘉辉
 *
 */
//@Entity
//@Table(name = "t_role_menu")
public class RoleMenu implements Serializable {

	private static final long serialVersionUID = 13158324154798260L;

	@Id
	@Column(name = "role_id")
	private Long roleId;

	@Id
	@Column(name = "menu_id")
	private Long menuId;

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public Long getMenuId() {
		return menuId;
	}

	public void setMenuId(Long menuId) {
		this.menuId = menuId;
	}

}
