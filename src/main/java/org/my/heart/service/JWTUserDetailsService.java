package org.my.heart.service;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.my.heart.dao.UserRepository;
import org.my.heart.entity.role.Role;
import org.my.heart.entity.user.JWTUser;
import org.my.heart.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("jwtUserDetailsService")
public class JWTUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// 通过username查询对应的用户信息，下面是JPA的条件查询写法
		Optional<User> userOptional = userRepository.findOne(new Specification<User>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				Predicate equal = criteriaBuilder.equal(root.get("username"), username);
				return equal;
			}
		});
		User user = userOptional.get();
		
		// 生成用户信息，提供给Spring Security进行校验
		JWTUser jwtUser = JWTUser.build()
									.setId(user.getId())
									.setUsername(username)
									.setName(user.getName())
									.setPassword(user.getPassword())
									.setNonLock(user.getNonLock())
									.setGredentialsNonExpired(user.getGredentialsNonExpired())
									.setEnabled(user.getEnabled());
		// 读取角色数据
		List<Role> roles = user.getRoles();
		if (roles != null && roles.size() > 0) {
			String[] roleIdArr = new String[roles.size()];
			for (int i = 0; i < roles.size(); i++) {
				roleIdArr[i] = roles.get(i).getId().toString();
			}
			jwtUser.setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils.join(roleIdArr, ",")));
		}
		return jwtUser;
	}

}
