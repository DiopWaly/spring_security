package sn.sun.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.websocket.server.PathParam;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import sn.sun.dao.ConfirmationTokenRepository;
import sn.sun.entities.AppRole;
import sn.sun.entities.AppUser;
import sn.sun.entities.ChangePassWord;
import sn.sun.entities.ConfirmationToken;
import sn.sun.entities.Response;
import sn.sun.entities.UserRole;
import sn.sun.sec.JWTAuthenticationFilter;
import sn.sun.sec.SecurityConstants;
import sn.sun.service.AccountService;
import sn.sun.service.AccountServiceImpl;

@RestController
//@RequestMapping("/api")
@Slf4j
public class AccountRestController {
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private ConfirmationTokenRepository confirmTokenRep;
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	@PostMapping("/register")
	public ResponseEntity<Response> register(@RequestBody @Valid RegisterForm userForm) {
		if(!userForm.getPassword().equals(userForm.getRepassword()))
			throw new RuntimeException("Vous n'avez pas confirmer le mot de passe");
		AppUser user = accountService.findUserByUsername(userForm.getUsername());
		
		if(user != null)
			throw new RuntimeException("Cet utilisteur existe déjas");
		AppUser appUser =  new AppUser();
		appUser.setUsername(userForm.getUsername());
		appUser.setPassword(userForm.getPassword());
		appUser.setEmail(userForm.getEmail());
		appUser.setEnabled(false);
		appUser.setLocked(false);
		appUser.setImgProfil("images/default.png");
		accountService.saveUser(appUser);
		accountService.addRoleToUser(userForm.getUsername(), "USER");
		//token validation
		String token = UUID.randomUUID().toString();
		ConfirmationToken confirmTk = new ConfirmationToken();
		confirmTk.setToken(token);
		confirmTk.setCreatedAt(LocalDateTime.now());
		confirmTk.setExpiresAt(LocalDateTime.now().plusMinutes(15));
		confirmTk.setAppUser(appUser);
		confirmTokenRep.save(confirmTk);
		
		return ResponseEntity.ok(
				Response.builder()
				.response(Collections.singletonMap("users",appUser))
				.message("User create successfull")
				.status(HttpStatus.CREATED)
				.statusCode(HttpStatus.CREATED.value())
				.timeStamp(LocalDateTime.now())
				.build()
		);
	}
	@GetMapping("/users")
	public ResponseEntity<Response> getRegisters(@RequestParam(value = "offset", required = false) int offset ) {
		log.info("message");
//		ResponseEntity<Response>
		return ResponseEntity.ok(
				Response.builder()
				.response(Collections.singletonMap("users",accountService.findAllUser(offset)))
				.message("listesdes utilisteurs")
				.status(HttpStatus.OK)
				.statusCode(HttpStatus.OK.value())
				.timeStamp(LocalDateTime.now())
				.build()
		);
//		return ResponseEntity.ok(
//					Response.builder()
//					.data("",accountService.findAllUser())
//				);
	}
	@GetMapping("/users/{id}")
	public AppUser getRegister(@PathVariable Long id) {
		return accountService.findUser(id);
	}
	
	@PutMapping("/user/role")
	public AppUser addRoleToUser(@RequestBody UserRole userRole) {
		AppUser user = accountService.findUserByUsername(userRole.getUsername());
		accountService.addRoleToUser(userRole.getUsername(), userRole.getRolename());
		return user;
	}
	@DeleteMapping("/user/role")
	public AppUser deleteRoleToUser(@RequestBody UserRole userRole) {
		return accountService.deleteRoleToUser(userRole.getUsername(), userRole.getRolename());
	}
	@GetMapping("/roles")
	public List<AppRole> findAllRole() {
		return accountService.findAllRole();
	}
	@PostMapping("/roles")
	public AppRole saveRole(@RequestBody AppRole role) {
		return accountService.saveRole(role);
	}
	@PostMapping("/reset_password")
	public String resetPassword(@RequestBody AppUser email, HttpServletRequest request) {
		System.out.println("mail :"+email.getEmail());
		String token = RandomString.make(45);
		try {
			this.accountService.updateResetPasswordToken(token, email.getEmail());
			String resetPasswordLink = request.getRequestURL().toString();
			resetPasswordLink = resetPasswordLink.replace(request.getServletPath(), "");
			resetPasswordLink = resetPasswordLink + "/reset_password?token="+token;
			sendEmail(email.getEmail(), resetPasswordLink);
		} catch (NotFoundException | UnsupportedEncodingException | MessagingException e) {
			e.printStackTrace();
		}
		return token; 	
	}
	private void sendEmail(String email, String resetPasswordLink) throws UnsupportedEncodingException, MessagingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		helper.setFrom("contact@yopmail.com", "cordialement waly");
		helper.setTo(email);
		String subject = "Voici le lien de reset password !!!";
		String content = "<p>Salut,</p>"
				+ "<p>vous avez un requéte dereset password.</p>"
				+"<p>Cliquer sur ce lien pour changer votre mot de passe : </p>"
				+"<p><b><a href=\""+resetPasswordLink+"\">Changer votre mot de passe</a></b></p>"
				+"<p>Ignor le mail si vous rappelé  de votre mot de pass !!!</p>";
		helper.setSubject(subject);
		helper.setText(content,true);
		javaMailSender.send(message);
	}
	
	@PostMapping("/reset")
	public ResponseEntity<Response> reset(@RequestBody AppUser userToken) {
		System.out.println("user :"+userToken.getRestPasswordToken());
		AppUser user = accountService.findByResetPasswordToken(userToken.getRestPasswordToken());
		if( user == null)
			return ResponseEntity.ok(
				Response.builder()
				.message("Token invalide")
				.status(HttpStatus.NOT_FOUND)
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.timeStamp(LocalDateTime.now())
				.build()
			);
		accountService.updatePassword(user, userToken.getPassword());
		return ResponseEntity.ok(
				Response.builder()
				.message("mot de passe reset avec succés")
				.status(HttpStatus.OK)
				.statusCode(HttpStatus.OK.value())
				.timeStamp(LocalDateTime.now())
				.build()
		);
	}
	@PostMapping("/change_pwd")
	public ResponseEntity<Response> changePwd(@RequestBody @Valid ChangePassWord body, HttpServletRequest request) {
		HashMap<String, String> map = (HashMap<String, String>) accountService.changePWD(body, request);;
		return ResponseEntity.ok(
				Response.builder()
				.message(map.get("message"))
//				.status(HttpStatus.OK)
				.statusCode(Integer.parseInt(map.get("code")))
				.timeStamp(LocalDateTime.now())
				.build()
				);
	}
	@GetMapping("/register/redirect")
	public void redirectTo(HttpServletResponse response){
		log.info("/register/waly");
		try {
			response.sendRedirect("https://google.com");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	@PostMapping("/refresh/token")
	public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException {
		String authorizationHeader = request.getHeader(SecurityConstants.HEADER_STRING);
		if(authorizationHeader != null && authorizationHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
			try {
				String refresh_token = authorizationHeader.substring(SecurityConstants.TOKEN_PREFIX.length());
				Claims claims = Jwts.parser()
					.setSigningKey(SecurityConstants.SECRET)
					.parseClaimsJws(authorizationHeader.substring(SecurityConstants.TOKEN_PREFIX.length()))
					.getBody();
				String username = claims.getSubject();
				AppUser user = accountService.findUserByUsername(username);
				String access_token = Jwts.builder()
					  .setSubject(user.getUsername())
					  .setExpiration( new Date(System.currentTimeMillis()+SecurityConstants.EXPIRATION_TIME))
					  .signWith(SignatureAlgorithm.HS256, SecurityConstants.SECRET)
					  .claim("roles", user.getRoles())
					  .compact();
				response.addHeader(SecurityConstants.HEADER_STRING,
						SecurityConstants.TOKEN_PREFIX+access_token);
				response.setHeader("refresh_Token", refresh_token);
				Map<String, String> tokens = new HashMap<>();
				tokens.put("acces_token", SecurityConstants.TOKEN_PREFIX+access_token);
				tokens.put("refresh_token",refresh_token);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				new ObjectMapper().writeValue(response.getOutputStream(), tokens);
			} catch (Exception e) {
				log.info("Error login in : {}",e.getMessage());
				response .setHeader("error", e.getMessage());
//				response.sendError(HttpStatus.FORBIDDEN.value());
				Map<String, String> error = new HashMap<>();
				error.put("error_message",e.getMessage());
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				new ObjectMapper().writeValue(response.getOutputStream(), error);

			}
		}else {
			throw new RuntimeException("Refresh token is missing");
		}
		return null;
	}
	@PostMapping("/upload_file")
	public ResponseEntity<Response> uploadFile(@RequestParam("file") MultipartFile file){
		return accountService.uploadFile(file);
	}
	@GetMapping(path="/images/{fileName}", produces=MediaType.IMAGE_PNG_VALUE)
	public byte[] getImage(@PathVariable("fileName") String fileName) throws IOException {
		log.info("c'est les images ");
		return Files.readAllBytes(Paths.get("C:\\Users\\HP 8th\\Documents\\workspace-spring-tool-suite-4-4.5.0.RELEASE\\jwt-sspring-sec\\src\\main\\resources\\static\\fileUpload\\"+fileName));
//		return Files.readAllBytes(Paths.get(System.getProperty("user.home")+"/Downloads/"+fileName));
	}
	@GetMapping("/register/confirm")
	public String confirmToken(@PathParam(value="token") String token) {
		log.info("c'est les token : {}",token);
		System.out.println("token :"+confirmTokenRep.findByToken(token));
		ConfirmationToken confirmationToken = accountService.getToken(token)
				.orElseThrow(() -> new IllegalStateException("Token not found"));
		if(confirmationToken.getConfirmedAt() != null)
			throw new IllegalStateException("email already confirmed");
		LocalDateTime expiredAt = confirmationToken.getExpiresAt();
		if(expiredAt.isBefore(LocalDateTime.now()))
			throw new IllegalStateException("token expired");
		confirmationToken.setConfirmedAt(LocalDateTime.now());
		AppUser userConfirmed = confirmationToken.getAppUser();
		userConfirmed.setEnabled(true);
		accountService.saveUser(userConfirmed);
		return "confirmed";
	}
	
}
