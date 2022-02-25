package sn.sun.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import sn.sun.entities.Task;

@RepositoryRestResource
public interface TaskRpository extends JpaRepository<Task, Long> {

}
