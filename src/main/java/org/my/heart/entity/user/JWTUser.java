package org.my.heart.entity.user;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class JWTUser implements UserDetails {

	private static final long serialVersionUID = -8259845930972485319L;

	private Long id;

	private String username;

	private String name;

	private String password;

	private List<GrantedAuthority> authorities;

	private Boolean nonLock;

	private Boolean gredentialsNonExpired;

	private Boolean enabled;

	public static JWTUser build() {
		return new JWTUser();
	}

	public Long getId() {
		return id;
	}

	public JWTUser setId(Long id) {
		this.id = id;
		return this;
	}

	public JWTUser setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getName() {
		return name;
	}

	public JWTUser setName(String name) {
		this.name = name;
		return this;
	}

	public JWTUser setPassword(String password) {
		this.password = password;
		return this;
	}

	public JWTUser setAuthorities(List<GrantedAuthority> authorities) {
		this.authorities = authorities;
		return this;
	}

	public JWTUser setNonLock(Boolean nonLock) {
		this.nonLock = nonLock;
		return this;
	}

	public JWTUser setGredentialsNonExpired(Boolean gredentialsNonExpired) {
		this.gredentialsNonExpired = gredentialsNonExpired;
		return this;
	}

	public JWTUser setEnabled(Boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.nonLock;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.gredentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

}
