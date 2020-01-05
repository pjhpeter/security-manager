package org.my.heart.controller;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;

import org.my.heart.dao.MenuViewRepository;
import org.my.heart.dao.UserRepository;
import org.my.heart.entity.Result;
import org.my.heart.entity.menu.MenuView;
import org.my.heart.entity.user.JWTUser;
import org.my.heart.entity.user.User;
import org.my.heart.entity.user.UserInfo;
import org.my.heart.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MenuViewRepository menuViewRepository;

	@GetMapping("/user/info")
	public Result getUserInfo(HttpServletRequest request) {
		JWTUser jwtUser = JWTUtils.parseToken(JWTUtils.getToken(request));
		User user = userRepository.getOne(jwtUser.getId());
		UserInfo userInfo = new UserInfo();
		BeanCopier.create(User.class, UserInfo.class, false).copy(user, userInfo, null);
		return Result.ok("获取成功", userInfo);
	}

	@GetMapping("/role/menu/{parentId}")
	public Result getMenu(HttpServletRequest request, @PathVariable("parentId") Long parentId) {
		JWTUser jwtUser = JWTUtils.parseToken(JWTUtils.getToken(request));
		Collection<? extends GrantedAuthority> authorities = jwtUser.getAuthorities();
		if (authorities != null && authorities.size() > 0) {
			List<MenuView> menuViews = menuViewRepository.findAll(new Specification<MenuView>() {

				private static final long serialVersionUID = 1671820077667449464L;

				@Override
				public Predicate toPredicate(Root<MenuView> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
					Set<String> roleIdSet = AuthorityUtils.authorityListToSet(authorities);
					System.out.println(roleIdSet);
					Predicate equal = criteriaBuilder.equal(root.get("parentId"), parentId);
					Predicate in = root.get("roleId").in(AuthorityUtils.authorityListToSet(authorities));
					return criteriaBuilder.and(equal, in);
				}
			});

			return Result.ok("获取成功", menuViews);
		}
		return Result.ok("该角色没有任何权限");
	}
}
