package sn.sun.web;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class RegisterForm {
	@NotEmpty(message = "user name must be null")
	private String username;
	private String email;
	private String password;
	private String repassword;
}
