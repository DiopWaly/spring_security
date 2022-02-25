package sn.sun.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import sn.sun.entities.AppRole;

public interface RoleRepository extends JpaRepository<AppRole, Long>{
	public AppRole findByRoleName(String roleName); 
}
