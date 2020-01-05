package org.my.heart.entity.menu;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 菜单栏视图
 * 
 * @author 彭嘉辉
 *
 */
@Entity
@Table(name = "v_menu_view")
// 创建视图：create or replace view v_menu_view as select m.*,r.id as role_id,max(rm.editable) as editable from t_menu m, t_role r, t_role_menu rm where m.id = rm.menu_id and r.id = rm.role_id group by m.id
public class MenuView implements Serializable {

	private static final long serialVersionUID = -4211068137377435000L;

	@Id
	@Column(insertable = false, updatable = false)
	private Long id;

	@Column(name = "menu_name", length = 64, insertable = false, updatable = false)
	private String menuName;

	@Column(name = "menu_uri", length = 64, insertable = false, updatable = false)
	private String menuURI;

	@Column(name = "menu_icon", length = 64, insertable = false, updatable = false)
	private String menuIcon;

	@Column(name = "parent_id", insertable = false, updatable = false)
	private Long parentId;

	@Column(name = "role_id", insertable = false, updatable = false)
	private Long roleId;

	@Column
	private Boolean editable;

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

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

}
