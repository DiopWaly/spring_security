package sn.sun.entities;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AppUser {
//	@SequenceGenerator(
//			name = "user_sequence",
//			sequenceName = "user_sequence",
//			allocationSize = 1
//	)
//	@GeneratedValue(
//			strategy = GenerationType.SEQUENCE
//			generator = "user_sequence"
//	)
	@Id @GeneratedValue
	private Long id;
	@Column(unique = true)
	@NotEmpty(message = "user name must be null")
	private String username;
	@Column(unique = true)
//	@NotEmpty(message = "Adresse mail must be null")
	private String email;
//	@JsonIgnore
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;
	private Boolean enabled;
	private Boolean locked;
	@Column(name = "rest_password_token")
	private String restPasswordToken;
	@Column(name = "img_profil")
	private String imgProfil;
	@ManyToMany(fetch = FetchType.EAGER)
	private Collection<AppRole> roles = new ArrayList<>();
}
