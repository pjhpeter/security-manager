package org.my.heart.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JWTAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 2937509166356305278L;
	
	private String token;
	private String ipAddress;

	public JWTAuthenticationToken(String token, String ipAddress) {
		super(null);
		this.token = token;
		this.ipAddress = ipAddress;
		setAuthenticated(false);
	}


	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return null;
	}

	public String getToken() {
		return token;
	}


	public String getIpAddress() {
		return ipAddress;
	}
	
}
