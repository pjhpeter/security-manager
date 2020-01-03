package org.my.heart.authentication;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.my.heart.constants.ContentType;
import org.my.heart.entity.LoginUser;
import org.my.heart.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 登录过滤器
 * 
 * @author 彭嘉辉
 *
 */
public class RequestBodyUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private static final Logger log = LoggerFactory.getLogger(RequestBodyUsernamePasswordAuthenticationFilter.class);

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		try {
			LoginUser loginUser = new ObjectMapper().readValue(request.getInputStream(), LoginUser.class);
			if (loginUser == null) {
				log.error("账号不存在");
				response.getWriter().write(Result.failure(HttpStatus.UNAUTHORIZED.value(), "账号不存在").toJSONString());
				throw new AuthenticationCredentialsNotFoundException("账号不存在");
			}
			log.debug("获取到用户名：" + loginUser.getUsername() + "，密码：" + loginUser.getPassword());
			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword());
			return this.getAuthenticationManager().authenticate(authRequest);
		} catch (Exception e) {
			log.error("登录异常，错误：" + e.getMessage());
			response.setContentType(ContentType.APPLICATION_JSON_UTF8);
			try {
				response.getWriter().write(Result.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "登录异常，请与管理员联系").toJSONString());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			throw new AuthorizationServiceException(e.getMessage());
		}
	}
}
