package sn.sun;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import sn.sun.dao.TaskRpository;
import sn.sun.entities.AppRole;
import sn.sun.entities.AppUser;
import sn.sun.entities.Task;
import sn.sun.service.AccountService;

import javax.servlet.http.HttpServletResponse;

@SpringBootApplication
@EnableScheduling
@AllArgsConstructor
public class JwtSspringSecApplication implements CommandLineRunner {
	//@Autowired
	private TaskRpository taskRep;
//	//@Autowired
//	private AccountService accountService;

	public static void main(String[] args) {
		SpringApplication.run(JwtSspringSecApplication.class, args);
	}
	@Bean
	public BCryptPasswordEncoder getBCPE() {
		return new BCryptPasswordEncoder();
	}

	public void run(String... args) throws Exception {
//		accountService.saveUser(new AppUser(null,"admin","1234",null));
//		accountService.saveUser(new AppUser(null,"user","1234",null));
//		accountService.saveRole(new AppRole(null, "ADMIN"));
//		accountService.saveRole(new AppRole(null, "USER"));
//		accountService.addRoleToUser("admin", "ADMIN");
//		accountService.addRoleToUser("admin", "USER");
//		accountService.addRoleToUser("user", "USER");
//		Stream.of("T1","T2","T3").forEach(t -> {
//			taskRep.save(new Task(null,t));
//		});
		taskRep.findAll().forEach(t -> {
			System.out.println(t.getTaskName());
		});
		System.out.println("path :"+Paths.get(System.getProperty("user.home")+"/Downloads/images/"));
	}
}
