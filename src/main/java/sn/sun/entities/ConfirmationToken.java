package sn.sun.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Data @NoArgsConstructor
public class ConfirmationToken {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(nullable = false)
	private String token;
	@Column(nullable = false)
	private LocalDateTime createdAt;
	@Column(nullable = false)
	private LocalDateTime expiresAt;
	private LocalDateTime confirmedAt;
	@ManyToOne
	@JoinColumn(nullable = false, name = "app_user_id")
	private AppUser appUser;

}
