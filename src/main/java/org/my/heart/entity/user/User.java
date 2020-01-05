package org.my.heart.entity.user;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.my.heart.entity.role.Role;

@Entity
@Table(name = "t_user")
public class User implements Serializable {

	private static final long serialVersionUID = -3388879385307332822L;

	@Id
	@Column
	private Long id;

	@Column(name = "username", length = 64, unique = true)
	private String username;

	@Column(name = "name", length = 64)
	private String name;

	@Column(name = "password", length = 64)
	private String password;

	@Column(name = "non_lock")
	private Boolean nonLock;

	@Column(name = "gredential_non_expired")
	private Boolean gredentialsNonExpired;

	@Column
	private Boolean enabled;

	@ManyToMany(targetEntity = Role.class, fetch = FetchType.LAZY)
	@JoinTable(name = "t_user_role", joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "role_id", referencedColumnName = "id") })
	private List<Role> roles;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getNonLock() {
		return nonLock;
	}

	public void setNonLock(Boolean nonLock) {
		this.nonLock = nonLock;
	}

	public Boolean getGredentialsNonExpired() {
		return gredentialsNonExpired;
	}

	public void setGredentialsNonExpired(Boolean gredentialsNonExpired) {
		this.gredentialsNonExpired = gredentialsNonExpired;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

}
