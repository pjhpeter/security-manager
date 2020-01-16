package org.my.heart.utils;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.my.heart.entity.Result;
import org.my.heart.utils.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class ExceptionResovler {
	
	/**
	 * 登录验证失败
	 * 
	 * @param response
	 * @param exception
	 * @throws IOException 
	 */
	public void handleAuthenticationException(HttpServletResponse response, AuthenticationException exception) throws IOException {
		ResponseUtils.buildResponseBody(response, Result.failure(HttpStatus.UNAUTHORIZED.value(), exception.getMessage()));
	}

	/**
	 * 权限验证失败
	 * 
	 * @param response
	 * @param exception
	 * @throws IOException 
	 */
	public void handleAccessDeniedException(HttpServletResponse response, AccessDeniedException exception) throws IOException {
		ResponseUtils.buildResponseBody(response, Result.failure(HttpStatus.FORBIDDEN.value(), exception.getMessage()));
	}
}
