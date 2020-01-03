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

@Configuration
@EnableWebSecurity
public class HeartSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService jwtUserDetailService;

	@Autowired
	private AuthenticationSuccessHandler jwtAuthenticationSuccessHandler;

	@Autowired
	private AuthenticationFailureHandler jwtAuthentiacionFailureHandler;

	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UsernamePasswordAuthenticationFilter jwtUsernamePasswordAuthenticationFilter() throws Exception {
		RequestBodyUsernamePasswordAuthenticationFilter jwtUsernamePasswordAuthenticationFilter = new RequestBodyUsernamePasswordAuthenticationFilter();
		// 不指定拦截路径就算替换了UsernamePasswordAuthenticaitonFilter都不会进来
		jwtUsernamePasswordAuthenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/user/login", HttpMethod.POST.name()));
		// 自定义UsernamePasswordAuthenticationFilter就会导致原来configure中的.successHandler和.failureHandler失效
		jwtUsernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(jwtAuthenticationSuccessHandler);
		jwtUsernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(jwtAuthentiacionFailureHandler);
		// 自定义filter必须指定AuthenticationManager
		jwtUsernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationManager());
		return jwtUsernamePasswordAuthenticationFilter;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(jwtUserDetailService);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.formLogin()
				.and()
				.addFilterAt(jwtUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
				.sessionManagement().disable()
				.csrf().ignoringRequestMatchers(new AntPathRequestMatcher("/user/login", HttpMethod.POST.name()));
	}
}
