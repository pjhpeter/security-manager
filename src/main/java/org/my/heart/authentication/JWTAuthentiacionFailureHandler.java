package org.my.heart.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.my.heart.constants.ContentType;
import org.my.heart.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component("jwtAuthentiacionFailureHandler")
public class JWTAuthentiacionFailureHandler implements AuthenticationFailureHandler {

	private static final Logger log = LoggerFactory.getLogger(JWTAuthentiacionFailureHandler.class);

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
		response.setContentType(ContentType.APPLICATION_JSON_UTF8);
		response.getWriter().write(Result.failure(401).setMessage(exception.getMessage()).toJSONString());
		log.debug("登录失败");
	}

}
