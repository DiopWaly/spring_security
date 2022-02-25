package sn.sun.service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javassist.NotFoundException;
import sn.sun.entities.AppRole;
import sn.sun.entities.AppUser;
import sn.sun.entities.ChangePassWord;
import sn.sun.entities.ConfirmationToken;
import sn.sun.entities.Response;

public interface AccountService {
	public AppUser saveUser(AppUser user);
	public AppRole saveRole(AppRole role);
	public void addRoleToUser(String username, String roleName);
	public AppUser deleteRoleToUser(String username, String roleName);
	public AppRole findRoleByRolename(String rolename);
	public AppUser findUserByUsername(String username);
	public List<AppUser> findAllUser(int limit);
	public List<AppRole> findAllRole();
	public AppUser findUser(Long id);
	public AppUser findByEmail(String email);
	public HashMap<?, ?> changePWD(ChangePassWord body, HttpServletRequest request);
	public void updateResetPasswordToken(String token, String email) throws NotFoundException;
	public AppUser findByResetPasswordToken(String token);
	public void updatePassword(AppUser user, String newPassword);
	public ResponseEntity<Response> uploadFile(MultipartFile file);
	public Optional<ConfirmationToken> getToken(String token);
}
