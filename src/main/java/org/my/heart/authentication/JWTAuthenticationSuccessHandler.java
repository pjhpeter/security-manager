package org.my.heart.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.my.heart.entity.Result;
import org.my.heart.entity.user.JWTUser;
import org.my.heart.utils.JWTUtils;
import org.my.heart.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component("jwtAuthenticationSuccessHandler")
public class JWTAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private static final Logger log = LoggerFactory.getLogger(JWTAuthenticationSuccessHandler.class);

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		JWTUser jwtUser = (JWTUser) authentication.getPrincipal();
		response.setHeader(JWTUtils.TOKEN_HEADER_NAME, JWTUtils.buildToken(jwtUser));
		ResponseUtils.buildResponseBody(response, Result.ok("登录成功"));
		log.debug("登录成功");
	}

}
