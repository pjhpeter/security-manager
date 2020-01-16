package org.my.heart.authentication;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.my.heart.entity.user.JWTUser;
import org.my.heart.service.JWTTokenHandler;
import org.my.heart.utils.ExceptionResovler;
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
		// 获取前端传来的token
		String token = jwtTokenHandler.getToken(request);
		if (StringUtils.isBlank(token)) {
			exceptionResovler.handleAuthenticationException(response, new AuthenticationCredentialsNotFoundException("请登录"));
			return;
		}
		try {
			// 解析token，如果token解析失败，这句代码会根据失败的类型抛出不同的异常，所以解析token即是验证token的合法性
			JWTUser jwtUser = jwtTokenHandler.parseToken(token);
			// 从缓存中读取最新的token
			String tokenFromCache = jwtTokenHandler.getTokenFromCache(jwtUser.getId());
			// 判断这个token是否失效
			if(!token.equals(tokenFromCache)) {
				// 前端传来的可能是老的token，从缓存中读取为过期的老token
				tokenFromCache = jwtTokenHandler.getTokenFromOldCache(token);
				if(StringUtils.isBlank(tokenFromCache)) {
					// 如果缓存中没有这个老token，说明已经失效了
					exceptionResovler.handleAuthenticationException(response, new CredentialsExpiredException("令牌已失效"));
					return;
				}
			}
			// 判断请求方的mac地址是否也token里的一致，防止劫持token发起的攻击
			if (jwtUser.getMacAddress().equals(IpUtils.getMacAddress(IpUtils.getIpAddress(request)))) {
				// token通过验证
				authorizeSuccess(token, jwtUser, response);
				filterChain.doFilter(request, response);
			} else {
				// token被劫持
				exceptionResovler.handleAuthenticationException(response, new BadCredentialsException("这样做是犯法的哦^_^"));
				return;
			}
		} catch (ExpiredJwtException e) {
			// 超过了30分钟，token过期
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
	
	/*
	 * token验证成功
	 */
	private void authorizeSuccess(String oldToken, JWTUser jwtUser, HttpServletResponse response) throws IOException {
		// 产生新token
		String newToken = jwtTokenHandler.buildToken(jwtUser);
		
		// 以老token为key缓存新token，期限为3秒，防止同时多个异步请求
		String tokenFromCache = jwtTokenHandler.getTokenFromOldCache(oldToken);
		if(StringUtils.isBlank(tokenFromCache)) {
			jwtTokenHandler.cacheOldToken(oldToken, newToken);
		}
		
		// 将新token写到响应头信息中
		response.setHeader(JWTTokenHandler.TOKEN_HEADER_NAME, newToken);
	}

}
