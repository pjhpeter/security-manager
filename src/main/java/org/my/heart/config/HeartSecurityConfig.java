package org.my.heart.config;

import org.my.heart.authentication.RequestBodyUsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
public class HeartSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService jwtUserDetailService;

	@Autowired
	private AuthenticationSuccessHandler jwtAuthenticationSuccessHandler;

	@Autowired
	private AuthenticationFailureHandler jwtAuthentiacionFailureHandler;
	
	@Autowired
	private OncePerRequestFilter jwtAuthenticationFilter;
	
	// 全局加密算法
	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// 登录验证过滤器
	@Bean
	public UsernamePasswordAuthenticationFilter requestBodyUsernamePasswordAuthenticationFilter() throws Exception {
		RequestBodyUsernamePasswordAuthenticationFilter filter = new RequestBodyUsernamePasswordAuthenticationFilter();
		// 不指定拦截路径就算替换了UsernamePasswordAuthenticaitonFilter都不会进来
		filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/user/login", HttpMethod.POST.name()));
		// 自定义UsernamePasswordAuthenticationFilter就会导致原来configure中的.successHandler和.failureHandler失效
		filter.setAuthenticationSuccessHandler(jwtAuthenticationSuccessHandler);
		filter.setAuthenticationFailureHandler(jwtAuthentiacionFailureHandler);
		// 自定义filter必须指定AuthenticationManager
		filter.setAuthenticationManager(authenticationManager());
		return filter;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(jwtUserDetailService);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.formLogin()
				.and()
				// 替换默认的用户名密码验证过滤器
				.addFilterAt(requestBodyUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
				.sessionManagement().disable()
				.csrf().disable()
				// 添加权限验证过滤器
				.addFilterAfter(jwtAuthenticationFilter, RequestBodyUsernamePasswordAuthenticationFilter.class);
	}
}
