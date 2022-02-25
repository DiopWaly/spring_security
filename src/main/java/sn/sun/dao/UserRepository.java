package sn.sun.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import sn.sun.entities.AppUser;
@RepositoryRestResource
public interface UserRepository extends JpaRepository<AppUser, Long> {
	public AppUser findByUsername(String username);
//	public AppUser findById(Long id);
	public AppUser findByEmail(String email);
	public AppUser findByRestPasswordToken(String token);
	
}
