package org.my.heart.authentication;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.my.heart.entity.Result;
import org.my.heart.utils.JWTUtils;
import org.my.heart.utils.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;

/**
 * 验证JWT token过滤器
 * 
 * @author 彭嘉辉
 *
 */
public class JWTAutenticationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		String token = JWTUtils.getToken(request);
		if (StringUtils.isBlank(token)) {
			ResponseUtils.buildResponseBody(response, Result.failure(HttpStatus.UNAUTHORIZED.value(), "请登录"));
			return;
		}
		try {
			JWTUtils.parseToken(token);
		} catch (ExpiredJwtException e) {
			ResponseUtils.buildResponseBody(response, Result.failure(HttpStatus.UNAUTHORIZED.value(), "令牌过期，请重新登录"));
			return;
		} catch (SignatureException e) {
			ResponseUtils.buildResponseBody(response, Result.failure(HttpStatus.UNAUTHORIZED.value(), "令牌错误，请重新登录"));
			return;
		}
		filterChain.doFilter(request, response);
	}

}
