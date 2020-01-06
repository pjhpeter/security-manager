package org.my.heart.service;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.my.heart.entity.user.JWTUser;
import org.my.heart.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * JWT工具类
 * 
 * @author 彭嘉辉
 *
 */
@Component("jwtTokenHandler")
@CacheConfig(cacheNames = JWTTokenHandler.TOKEN_CACHE_NAME)
public class JWTTokenHandler {

	private static final Logger log = LoggerFactory.getLogger(JWTTokenHandler.class);

	public static final String TOKEN_HEADER_NAME = "Authorization";

	public static final String TOKEN_CACHE_NAME = "token";

	private static final String SIGN_KEY = "by_heart";

	// token有效期30分钟
	private static final long TOKEN_VALIDITY_PERIOD = 1000 * 60 * 30;

	private static final String TOKEN_ISSUSER = "heart";

	private static final String TOKEN_PREFIX = "Bearer ";

	/**
	 * 生成token
	 * 
	 * @param user 用户信息
	 * @return token
	 */
	@CachePut(key = "#user.id")
	public String buildToken(JWTUser user) {
		log.debug(user.toString());
		JwtBuilder builder = Jwts.builder().setId(CommonUtils.generateId().toString()).setIssuer(TOKEN_ISSUSER).setAudience(user.getId().toString()).setSubject(user.getUsername()).setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY_PERIOD)).signWith(SignatureAlgorithm.HS256, SIGN_KEY);
		Map<String, Object> claims = new HashMap<>();

		// 添加mac地址绑定，防止token被截获后恶意攻击
		claims.put("mac", user.getMacAddress());

		// 处理权限
		Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
		if (authorities.size() > 0) {
			Set<String> authoritiesSet = AuthorityUtils.authorityListToSet(authorities);
			claims.put("roles", StringUtils.join(authoritiesSet, ","));
			builder.addClaims(claims);
		}
		return builder.compact();
	}

	/**
	 * 验证token
	 * 
	 * @param token 请求
	 * @return 用户信息
	 */
	public JWTUser parseToken(String token) throws ExpiredJwtException {
		token = token.trim().replaceAll(TOKEN_PREFIX, "");
		Claims claims = Jwts.parser().setSigningKey(SIGN_KEY).parseClaimsJws(token).getBody();
		log.debug("解析token：" + claims.getSubject());
		JWTUser jwtUser = JWTUser.build().setId(Long.parseLong(claims.getAudience())).setName(claims.getSubject()).setMacAddress(claims.get("mac").toString());
		if (claims.get("roles") != null) {
			jwtUser.setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList(claims.get("roles").toString()));
		}
		return jwtUser;

	}

	/**
	 * 从请求头中获取token
	 * 
	 * @param request 请求
	 * @return token
	 */
	public String getToken(HttpServletRequest request) {
		String token = request.getHeader(TOKEN_HEADER_NAME);
		if (StringUtils.isNotBlank(token)) {
			token = token.replace(TOKEN_PREFIX, "");
		}
		return token;
	}

	@Cacheable(key = "#userId")
	public String getTokenFromCache(Long userId) {
		log.trace("缓存中没有用户token");
		return "";
	}

	/**
	 * 检查缓存中是否有该老token，如果有则说明老token还没过期，仍然可以通过验证
	 * 
	 * @param token
	 * @return 老token
	 */
	@Cacheable(key = "#token")
	public String getOldTokenFromCache(String token) {
		log.trace("缓存中没有老token");
		return "";
	}

	@CacheEvict(key = "#token", beforeInvocation = true)
	public void removeOldTokenCache(String token) {
		System.out.println("删除老token");
		log.trace("没有删除老token缓存");
	}
	
	/**
	 * 以老token为key，做缓存，10秒过期，防止同时前端同时发送多个异步请求时，出现token已经过去无法通过验证的问题
	 * 
	 * @param token
	 */
	@CachePut(key = "#token")
	public String cacheOldToken(String token) {
		log.debug("缓存老token");
		new Thread(this.new removeOldTokenCacheExecutor(token, this)).start();
		return token;
	}

	public class removeOldTokenCacheExecutor implements Runnable {

		private String oldToken;
		
		private JWTTokenHandler jwtTokenHandler;

		public removeOldTokenCacheExecutor(String oldToken, JWTTokenHandler jwtUtils) {
			this.oldToken = oldToken;
			this.jwtTokenHandler = jwtUtils;
		}

		@Override
		public void run() {
			try {
				// 等待10秒
				Thread.sleep((10 * 1000));
				this.jwtTokenHandler.removeOldTokenCache(this.oldToken);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
