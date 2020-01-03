package org.my.heart.service;

import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.my.heart.dao.UserRepository;
import org.my.heart.entity.JWTUser;
import org.my.heart.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("jwtUserDetailsService")
public class JWTUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> userOptional = userRepository.findOne(new Specification<User>() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				Predicate equal = criteriaBuilder.equal(root.get("username"), username);
				return equal;
			}
		});
		User user = userOptional.get();
		return JWTUser.build()
				.setId(user.getId())
				.setUsername(username)
				.setName(user.getName())
				.setPassword(user.getPassword())
				.setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList("ADMIN"))
				.setNonLock(user.getNonLock())
				.setGredentialsNonExpired(user.getGredentialsNonExpired())
				.setEnabled(user.getEnabled());
	}
	
}
