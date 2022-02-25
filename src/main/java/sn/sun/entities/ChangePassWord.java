package sn.sun.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class ChangePassWord {
	private String oldPassword;
	private String newPassword;
	private String confirmPassword;
}
