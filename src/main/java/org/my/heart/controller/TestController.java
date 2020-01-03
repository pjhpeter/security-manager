package org.my.heart.controller;

import javax.servlet.http.HttpServletRequest;

import org.my.heart.dao.UserRepository;
import org.my.heart.entity.JWTUser;
import org.my.heart.entity.Result;
import org.my.heart.entity.User;
import org.my.heart.entity.UserInfo;
import org.my.heart.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/user/info")
	public Result getUserInfo(HttpServletRequest request) {
		JWTUser jwtUser = JWTUtils.parseToken(JWTUtils.getToken(request));
		User user = userRepository.getOne(jwtUser.getId());
		UserInfo userInfo = new UserInfo();
		BeanCopier.create(User.class, UserInfo.class, false).copy(user, userInfo, null);
		return Result.ok("获取成功", userInfo);
	}
}
