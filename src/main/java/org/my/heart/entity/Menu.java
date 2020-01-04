package org.my.heart.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 功能菜单
 * 
 * @author 彭嘉辉
 *
 */
@Entity
@Table(name = "t_menu")
public class Menu implements Serializable {

	private static final long serialVersionUID = -1501634849327254247L;

	@Id
	@Column
	private Long id;

	@Column(name ="menu_name", length = 64)
	private String menuName;

	@Column(name = "menu_uri", length = 64)
	private String menuURI;

	@Column(name = "menu_icon", length = 64)
	private String menuIcon;

	@Column(name = "parent_id")
	private Long parentId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMenuName() {
		return menuName;
	}

	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}

	public String getMenuURI() {
		return menuURI;
	}

	public void setMenuURI(String menuURI) {
		this.menuURI = menuURI;
	}

	public String getMenuIcon() {
		return menuIcon;
	}

	public void setMenuIcon(String menuIcon) {
		this.menuIcon = menuIcon;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

}
