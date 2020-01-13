package org.my.heart.authentication;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.my.heart.entity.user.JWTUser;
import org.my.heart.service.JWTTokenHandler;
import org.my.heart.utils.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;

/**
 * 验证JWT token过滤器
 * 
 * @author 彭嘉辉
 *
 */
@Component("jwtAuthenticationFilter")
public class JWTAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JWTTokenHandler jwtTokenHandler;
	
	@Autowired
	private ExceptionResovler exceptionResovler;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String token = jwtTokenHandler.getToken(request);
		if (StringUtils.isBlank(token)) {
			exceptionResovler.handleAuthenticationException(response, new AuthenticationCredentialsNotFoundException("请登录"));
			return;
		}
		try {
			JWTUser jwtUser = jwtTokenHandler.parseToken(token);
			String tokenFromCache = jwtTokenHandler.getTokenFromCache(jwtUser.getId());
			if(!token.equals(tokenFromCache)) {
				tokenFromCache = jwtTokenHandler.getTokenFromOldCache(token);
				if(StringUtils.isBlank(tokenFromCache)) {
					exceptionResovler.handleAuthenticationException(response, new CredentialsExpiredException("令牌已过期"));
					return;
				}
			}
			// 判断请求方的mac地址是否也token里的一致，截获token发起的攻击
			if (jwtUser.getMacAddress().equals(IpUtils.getMacAddress(IpUtils.getIpAddress(request)))) {
				authorizeSuccess(token, jwtUser, response);
				filterChain.doFilter(request, response);
			} else {
				// token被截获
				exceptionResovler.handleAuthenticationException(response, new BadCredentialsException("这样做是犯法的哦^_^"));
				return;
			}
		} catch (ExpiredJwtException e) {
			// token过期
			exceptionResovler.handleAuthenticationException(response, new CredentialsExpiredException("令牌过期，请重新登录"));
			return;
		} catch (SignatureException e) {
			// token被篡改
			exceptionResovler.handleAuthenticationException(response, new BadCredentialsException("这样做是犯法的哦^_^"));
			return;
		}catch (MalformedJwtException e) {
			// token格式异常
			exceptionResovler.handleAuthenticationException(response, new BadCredentialsException("这样做是犯法的哦^_^"));
			return;
		}
	}
	
	private void authorizeSuccess(String oldToken, JWTUser jwtUser, HttpServletResponse response) throws IOException {
		// 产生新token
		String newToken = jwtTokenHandler.buildToken(jwtUser);
		
		// 以老token为key缓存新token，期限为3秒，防止同时多个异步请求
		String tokenFromCache = jwtTokenHandler.getTokenFromOldCache(oldToken);
		if(StringUtils.isBlank(tokenFromCache)) {
			jwtTokenHandler.cacheOldToken(oldToken, newToken);
		}
		
		response.setHeader(JWTTokenHandler.TOKEN_HEADER_NAME, newToken);
	}

}
