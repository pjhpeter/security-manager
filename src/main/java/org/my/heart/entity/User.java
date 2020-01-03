package org.my.heart.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_user")
public class User implements Serializable {

	private static final long serialVersionUID = -3388879385307332822L;

	@Id
	@Column(name = "id")
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

}
