package org.my.heart.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.my.heart.entity.Result;
import org.my.heart.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * 登录失败处理
 * 
 * @author 彭嘉辉
 *
 */
@Component("jwtAuthentiacionFailureHandler")
public class JWTAuthentiacionFailureHandler implements AuthenticationFailureHandler {

	private static final Logger log = LoggerFactory.getLogger(JWTAuthentiacionFailureHandler.class);

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
		ResponseUtils.buildResponseBody(response, Result.failure(HttpStatus.UNAUTHORIZED.value()).setMessage(exception.getMessage()));
		log.debug("登录失败");
	}

}
