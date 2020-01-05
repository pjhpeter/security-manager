package org.my.heart.entity.user;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.my.heart.entity.menu.Menu;

/**
 * 权限
 * @author 彭嘉辉
 *
 */
//@Entity
//@Table(name = "t_user_role")
public class UserRole implements Serializable{

	private static final long serialVersionUID = 1821229127802319868L;

	@Id
	@Column(name = "user_id")
	private Long userId;
	
	@Id
	@Column(name = "role_id")
	private Long role_id;
	
	@ManyToMany(targetEntity = Menu.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name="menu_id", foreignKey = @ForeignKey)
	private List<Menu> menus;
	

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getRole_id() {
		return role_id;
	}

	public void setRole_id(Long role_id) {
		this.role_id = role_id;
	}

	public List<Menu> getMenus() {
		return menus;
	}

	public void setMenus(List<Menu> menus) {
		this.menus = menus;
	}
	
}
