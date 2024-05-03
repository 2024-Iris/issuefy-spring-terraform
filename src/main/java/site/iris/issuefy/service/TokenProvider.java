package site.iris.issuefy.service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenProvider {
	private Key key;

	public TokenProvider(@Value("${jwt.secretKey}") String secretKey) {
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
	}

	public String createToken(Map<String, Object> claims) {

		return Jwts.builder()
			.claims(claims)
			.expiration(getExpireDateAccessToken())
			.signWith(key)
			.compact();
	}

	public Claims getClaims(String token) {

		return Jwts.parser()
			.verifyWith((SecretKey)key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private Date getExpireDateAccessToken() {
		long expireTimeMils = 1000 * 60 * 60 * 8;

		return new Date(System.currentTimeMillis() + expireTimeMils);
	}
}
