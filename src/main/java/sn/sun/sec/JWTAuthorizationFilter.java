package sn.sun.sec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Slf4j
public class JWTAuthorizationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if(request.getServletPath().equals("login")) {
			filterChain.doFilter(request, response);
		}else {
			String authorizationHeader = request.getHeader(SecurityConstants.HEADER_STRING);
			if(authorizationHeader != null && authorizationHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
				try {
					Claims claims = Jwts.parser()
						.setSigningKey(SecurityConstants.SECRET)
						.parseClaimsJws(authorizationHeader.substring(SecurityConstants.TOKEN_PREFIX.length()))
						.getBody();
					String username = claims.getSubject();
					ArrayList<Map<String, String>> roles =(ArrayList<Map<String, String>>) claims.get("roles");
					Collection<GrantedAuthority> authorities = new ArrayList<>();
					roles.forEach(r -> {
						authorities.add(new SimpleGrantedAuthority(r.get("authority")));
					});
					UsernamePasswordAuthenticationToken authenticationToken = 
								new UsernamePasswordAuthenticationToken(username, null, authorities); 
					SecurityContextHolder .getContext().setAuthentication(authenticationToken);
					filterChain.doFilter(request, response);
				} catch (Exception e) {
					log.info("Error login in : {}",e.getMessage());
					response .setHeader("error", e.getMessage());
//					response.sendError(HttpStatus.FORBIDDEN.value());
					Map<String, String> error = new HashMap<>();
					error.put("error_message",e.getMessage());
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					new ObjectMapper().writeValue(response.getOutputStream(), error);

				}
			}else {
				filterChain.doFilter(request, response);
			}
		}
		
	}

	/**@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, "
				+ "Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization");
		response.addHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin, "
				+ "Access-Control-Allow-Credentials, authorization");
		if (request.getMethod().equals("OPTIONS")) {
			response.setStatus(HttpServletResponse.SC_OK);
		}
		
		String jwt = request.getHeader(SecurityConstants.HEADER_STRING);
		if(jwt ==  null || !jwt.startsWith(SecurityConstants.TOKEN_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}
		
		Claims claims = Jwts.parser()
			.setSigningKey(SecurityConstants.SECRET)
			.parseClaimsJws(jwt.replace(SecurityConstants.TOKEN_PREFIX, ""))
			.getBody();
		String username = claims.getSubject();
		System.out.println("username subject :"+username);
		ArrayList<Map<String, String>> roles =(ArrayList<Map<String, String>>) claims.get("roles");
		System.out.println("roles mapper :"+roles);
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		roles.forEach(r -> {
			authorities.add(new SimpleGrantedAuthority(r.get("authority")));
		});
		UsernamePasswordAuthenticationToken authenticatedUser =
				new UsernamePasswordAuthenticationToken(username, null, authorities);
	    SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
	    filterChain.doFilter(request, response);
	}*/

}
