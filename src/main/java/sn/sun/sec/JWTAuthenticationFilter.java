package sn.sun.sec;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import sn.sun.entities.AppUser;

@AllArgsConstructor
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter{
	
	private AuthenticationManager authenticationManager; 
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		AppUser appUser = null;
		try {
			appUser = new ObjectMapper().readValue(request.getInputStream(), AppUser.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		System.out.println("***************************");
		System.out.println("username :"+appUser.getUsername());
		System.out.println("password :"+appUser.getPassword());
		System.out.println("********************************");
		
		return authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(appUser.getUsername(), appUser.getPassword())
			);
	}
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		User springUser = (User)authResult.getPrincipal();
		Algorithm algorithm = Algorithm.HMAC256(SecurityConstants.SECRET.getBytes());
//		String jwtToken = JWT.create()
//				.withSubject(springUser.getUsername())
//				.withExpiresAt(new Date(System.currentTimeMillis()+SecurityConstants.EXPIRATION_TIME))
//				.withIssuer(request.getRequestURL().toString())
//				.withClaim("roles", springUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
//				.sign(algorithm);
		String jwtToken = Jwts.builder()
							  .setSubject(springUser.getUsername())
							  .setExpiration( new Date(System.currentTimeMillis()+SecurityConstants.EXPIRATION_TIME) )
							  .signWith(SignatureAlgorithm.HS256, SecurityConstants.SECRET)
							  .claim("roles", springUser.getAuthorities())
							  .compact();
		String refresh_Token = Jwts.builder()
				  .setSubject(springUser.getUsername())
				  .setExpiration( new Date(System.currentTimeMillis()+SecurityConstants.EXPIRATION_TIME*5) )
				  .signWith(SignatureAlgorithm.HS256, SecurityConstants.SECRET)
				  .compact();
		
		System.out.println("jwtToken "+jwtToken);
		System.out.println("refresh_Token "+refresh_Token);
		response.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX+jwtToken);
		response.setHeader("refresh_Token", refresh_Token);
		Map<String, String> tokens = new HashMap<>();
		tokens.put("acces_token", SecurityConstants.TOKEN_PREFIX+jwtToken);
		tokens.put("refresh_token",refresh_Token);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		new ObjectMapper().writeValue(response.getOutputStream(), tokens);
		super.successfulAuthentication(request, response, chain, authResult);
	}

}
