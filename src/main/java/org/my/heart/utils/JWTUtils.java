package org.my.heart.utils;

import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.my.heart.entity.JWTUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

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
public class JWTUtils {

	private static final Logger log = LoggerFactory.getLogger(JWTUtils.class);

	public static final String TOKEN_HEADER_NAME = "Authorization";

	private static final String SIGN_KEY = "by_heart";

	private static final long TOKEN_VALIDITY_PERIOD = 1000 * 60 * 10;

	private static final String TOKEN_ISSUSER = "heart";

	private static final String TOKEN_PREFIX = "Bearer ";

	/**
	 * 生成token
	 * 
	 * @param user 用户信息
	 * @return token
	 */
	public static String buildToken(JWTUser user) {
		log.debug(user.toString());
		Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
		System.out.println(authorities);
		JwtBuilder builder = Jwts.builder()
									.setId(CommonUtils.generateId().toString())
									.setIssuer(TOKEN_ISSUSER)
									.setAudience(user.getId().toString())
									.setSubject(user.getUsername())
									.setIssuedAt(new Date())
									.setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY_PERIOD))
									.signWith(SignatureAlgorithm.HS256, SIGN_KEY);
		return builder.compact();
	}

	/**
	 * 验证token
	 * 
	 * @param token 请求
	 * @return 用户信息
	 */
	public static JWTUser parseToken(String token) throws ExpiredJwtException {
		token = token.trim().replaceAll(TOKEN_PREFIX, "");
		Claims claims = Jwts.parser().setSigningKey(SIGN_KEY).parseClaimsJws(token).getBody();
		log.debug("解析token：" + claims.getSubject());
		return JWTUser.build()
						.setId(Long.parseLong(claims.getAudience()))
						.setName(claims.getSubject());
	}

	/**
	 * 从请求头中获取token
	 * 
	 * @param request 请求
	 * @return token
	 */
	public static String getToken(HttpServletRequest request) {
		String token = request.getHeader(TOKEN_HEADER_NAME);
		if (StringUtils.isNotBlank(token)) {
			token = token.replace(TOKEN_PREFIX, "");
		}
		return token;
	}
	
}
