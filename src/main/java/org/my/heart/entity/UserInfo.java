package org.my.heart.entity;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

/**
 * 登录成功后返回的用户信息
 * 
 * @author 彭嘉辉
 *
 */
public class UserInfo implements Serializable {

	private static final long serialVersionUID = 3667955655744391811L;

	private Long id;

	private String username;

	private String name;

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

	public String toJSONString() {
		return JSON.toJSONString(this);
	}
}
