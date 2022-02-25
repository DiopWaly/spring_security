package sn.sun.web;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import sn.sun.dao.TaskRpository;
import sn.sun.entities.Response;
import sn.sun.entities.Task;

@RestController
public class TaskRestController {
	@Autowired
	private TaskRpository taskRep;
	
//	@GetMapping("/tasks")
//	public List<Task> listTasks(){
//		return taskRep.findAll();
//	}
	
	@GetMapping("/tasks")
	public ResponseEntity<Response> tasks(){
		return ResponseEntity.ok(
					Response.builder()
					.timeStamp(LocalDateTime.now())
					.response(Collections.singletonMap("data",taskRep.findAll()))
					.message("tasks retrived")
					.status(HttpStatus.OK)
					.statusCode(HttpStatus.OK.value())
					.build()
			   );
	}
	
	@PostMapping("/tasks")
	public Task saveTask(@RequestBody Task task){
		return taskRep.save(task);
	}

}
