package sn.sun.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;

import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotEmpty(message = "Task name ne peut etre null")
	private String taskName;

}
