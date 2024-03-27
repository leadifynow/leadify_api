package com.api.leadify.jwt;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import com.api.leadify.entity.SessionM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


public class JWT {
	private static final Logger LOGGER = LoggerFactory.getLogger(JWT.class);
	public static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60; // 24 hours
	public static final String HEADER = "Authorization";
	public static final String PREFIX = "Bearer ";
	public static final  String SECRETKEY = "Hay que trabajar, hay que aprender, hay que comer, hay que descansar y también hay que jugar.";
	
    public static String getJWTToken(String username, int idUsuario) {
		List<GrantedAuthority> grantedAuthorities = AuthorityUtils
				.commaSeparatedStringToAuthorityList(SECRETKEY);
		String token = Jwts
				.builder()
				.setId(String.valueOf(idUsuario))
				.setSubject(username)
				.claim("authorities",
						grantedAuthorities.stream()
								.map(GrantedAuthority::getAuthority)
								.collect(Collectors.toList()))
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() +  JWT_TOKEN_VALIDITY * 1000))
				.signWith(SignatureAlgorithm.HS512,
						SECRETKEY.getBytes()).compact();

		return token;
	}

	public static SessionM getSession(HttpServletRequest request) {
		SessionM sessionM = new SessionM();
		String authenticationHeader = request.getHeader(HEADER);
		if (authenticationHeader == null) {
			sessionM.idUsuario = -999;
			sessionM.email = "NO ACCESS TOKEN";
			sessionM.token = "NO ACCESS TOKEN";
			sessionM.status = -9;
			return sessionM;
		}
		String token = request.getHeader(HEADER).replace(PREFIX, "");
		try {
			Claims claims = Jwts.parser().setSigningKey(SECRETKEY.getBytes()).parseClaimsJws(token).getBody();
			// LOGGER.info("Token expiration time: {}", claims.getExpiration()); // Log expiration time
			long timeLeft = claims.getExpiration().getTime() - System.currentTimeMillis();
			long minutesLeft = timeLeft / (60 * 1000);
			// LOGGER.info("Minutes left for expiration: {}", minutesLeft);
			if (claims.getExpiration().before(new Date())) {
				// LOGGER.info("TOKEN EXPIRADO!");
				sessionM.idUsuario = -999;
				sessionM.email = "TOKEN EXPIRADO";
				sessionM.token = "TOKEN EXPIRADO";
				sessionM.status = -9;

			} else {
				sessionM.idUsuario = Integer.parseInt(claims.getId());
				sessionM.email = claims.getSubject();
				sessionM.status = 0;
				sessionM.token = token;
				sessionM.expiration = claims.getExpiration(); // Add expiration time to SessionM
				// LOGGER.info("Token expiration time: {}", claims.getExpiration()); // Log expiration time
			}

		} catch(Exception e) {
			// LOGGER.info("TOKEN no válido" );
			// LOGGER.info("TOKEN EXPIRADO!");
			sessionM.idUsuario = -999;
			sessionM.email = "TOKEN EXPIRADO";
			sessionM.token = "TOKEN EXPIRADO";
			sessionM.status = -9;
		}
		return sessionM;
	}

	public static SessionM getTokenInfo(String token) {
    	SessionM sessionM = new SessionM();
    	try {
			Claims claims = Jwts.parser().setSigningKey(SECRETKEY.getBytes()).parseClaimsJws(token).getBody();
			// LOGGER.info("Claims Ok" );
			// LOGGER.info("Authorities:" + claims.get("authorities"));
			// LOGGER.info("Username:" + claims.getSubject());
			// LOGGER.info("ID:" + claims.getId());
			// LOGGER.info("Expiration:" + claims.getExpiration().toString());
			if (claims.getExpiration().before(new Date())) {
				LOGGER.info("TOKEN EXPIRADO!");
				sessionM.idUsuario = -999;
				sessionM.email = "TOKEN EXPIRADO";
				sessionM.token = "TOKEN EXPIRADO";
				sessionM.status = -9;
				
			} else {
				sessionM.idUsuario = Integer.parseInt(claims.getId());
				sessionM.email = claims.getSubject();
				sessionM.status = 0;
				sessionM.token = token;
				sessionM.expiration =  claims.getExpiration();
			}
			
		} catch(Exception e) {
			LOGGER.info("TOKEN no válido" );
			
		}
    	return sessionM;
    }
}
