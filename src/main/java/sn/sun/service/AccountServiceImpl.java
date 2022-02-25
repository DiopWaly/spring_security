package sn.sun.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sn.sun.dao.ConfirmationTokenRepository;
import sn.sun.dao.RoleRepository;
import sn.sun.dao.UserRepository;
import sn.sun.entities.AppRole;
import sn.sun.entities.AppUser;
import sn.sun.entities.ChangePassWord;
import sn.sun.entities.ConfirmationToken;
import sn.sun.entities.Response;
import sn.sun.sec.SecurityConstants;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class AccountServiceImpl implements AccountService {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private ConfirmationTokenRepository confirmationTokenRepository;

	public AppUser saveUser(AppUser user) {
		String hashPW = bCryptPasswordEncoder.encode(user.getPassword());
		user.setPassword(hashPW);
		return userRepository.save(user);
	}

	public AppRole saveRole(AppRole role) {
		return roleRepository.save(role);
	}

	public void addRoleToUser(String username, String roleName) {
		AppRole role = roleRepository.findByRoleName(roleName);
		AppUser user = userRepository.findByUsername(username);
		user.getRoles().add(role);
		
	}

	public AppUser findUserByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	public List<AppUser> findAllUser(int limit) {
		return userRepository.findAll(PageRequest.of(0, limit)).toList();
	}

	public AppUser findUser(Long id) {
		return userRepository.findById(id).get();
	}

	public AppRole findRoleByRolename(String rolename) {
		return roleRepository.findByRoleName(rolename);
	}

	public AppUser deleteRoleToUser(String username, String roleName) {
		AppUser user = userRepository.findByUsername(username);
		AppRole role = roleRepository.findByRoleName(roleName);
		user.getRoles().remove(role);
		return user;
	}

	public List<AppRole> findAllRole() {
		return roleRepository.findAll();
	}
	private String setImageUrl() {
		String[] imgNames = {"image1.png","image2.png","image3.png"};
		return ServletUriComponentsBuilder.fromCurrentContextPath().path("/server/images/" + imgNames[new Random().nextInt(3)]).toString();
	}
	
	public AppUser findByEmail(String email) {
		return this.userRepository.findByEmail(email);
	}
	
	public HashMap<String, String> changePWD(ChangePassWord body, HttpServletRequest request) {		
		HashMap<String, String> map = new HashMap<>();
		if(!body.getNewPassword().equals(body.getConfirmPassword())) {
			map.put("code", "400");
			map.put("message", "Vous n'avez pas confirmer le mot de passe");
			return map;
		}
//			throw new RuntimeException("Vous n'avez pas confirmer le mot de passe");
		
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		Claims claims = Jwts.parser()
				.setSigningKey(SecurityConstants.SECRET)
				.parseClaimsJws(token.replace(SecurityConstants.TOKEN_PREFIX, ""))
				.getBody();
		String username = claims.getSubject();
		AppUser user = this.userRepository.findByUsername(username);
		if(user == null) {
			map.put("code", "404");
			map.put("message", "Cet utilisteur n'existe pas");
			return map;
		}
		if(!bCryptPasswordEncoder.matches(body.getOldPassword(), user.getPassword())) {
			map.put("code", "400");
			map.put("message", "Mot de passe incorrecte");
			return map;
		}
//			throw new RuntimeException("Mot de passe incorrecte");
		user.setPassword(bCryptPasswordEncoder.encode(body.getNewPassword()));
		map.put("code", "200");
		map.put("message", "Changement de mot de passe effectué avec succes");
		return map;
	}

	@Override
	public void updateResetPasswordToken(String token, String email) throws NotFoundException {
		AppUser user = this.findByEmail(email);
		System.out.println("user :"+user);
		if(user != null) {
			user.setRestPasswordToken(token);
			this.userRepository.save(user);
		}else {
			throw new NotFoundException("utilisateur avec l'adresse mail "+email+" n'existe pas.");
		}
	}

	@Override
	public AppUser findByResetPasswordToken(String token) {
		return userRepository.findByRestPasswordToken(token);
	}

	@Override
	public void updatePassword(AppUser user, String newPassword) {
		BCryptPasswordEncoder passwordEncode = new BCryptPasswordEncoder();
		newPassword = passwordEncode.encode(newPassword);
		user.setPassword(newPassword);
		user.setRestPasswordToken(null);
		userRepository.save(user);	
	}

	@Override
	public ResponseEntity<Response> uploadFile(MultipartFile file) {
		try {
			file.transferTo(new File("C:\\Users\\HP 8th\\Documents\\workspace-spring-tool-suite-4-4.5.0.RELEASE\\jwt-sspring-sec\\src\\main\\resources\\static\\fileUpload\\"+file.getOriginalFilename()));
		} catch (IllegalStateException | IOException e) {
			return ResponseEntity.ok(
					Response.builder()
					.message("Error")
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.statusCode(HttpStatus.FAILED_DEPENDENCY.value())
					.timeStamp(LocalDateTime.now())
					.build()
				);
		}
		return ResponseEntity.ok(
				Response.builder()
				.message("Succes")
				.status(HttpStatus.OK)
				.statusCode(HttpStatus.CREATED.value())
				.timeStamp(LocalDateTime.now())
				.build()
			);
	}

	@Override
	public Optional<ConfirmationToken> getToken(String token) {
		return confirmationTokenRepository.findByToken(token);
	}
	
	
}
