package org.my.heart.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.my.heart.entity.Result;
import org.my.heart.entity.user.JWTUser;
import org.my.heart.service.JWTTokenHandler;
import org.my.heart.utils.IpUtils;
import org.my.heart.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component("jwtAuthenticationSuccessHandler")
public class JWTAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private static final Logger log = LoggerFactory.getLogger(JWTAuthenticationSuccessHandler.class);

	@Autowired
	private JWTTokenHandler jwtTokenHandler;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		JWTUser jwtUser = (JWTUser) authentication.getPrincipal();
		String ipAddress = IpUtils.getIpAddress(request);
		String macAddress = IpUtils.getMacAddress(ipAddress);
		response.setHeader(JWTTokenHandler.TOKEN_HEADER_NAME, jwtTokenHandler.buildToken(jwtUser.setMacAddress(macAddress)));
		ResponseUtils.buildResponseBody(response, Result.ok("登录成功"));
		log.debug("登录成功");
	}

}
