package sn.sun;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import sn.sun.entities.AppUser;
import sn.sun.service.AccountService;

@Component
public class ScheduledTasks {
	@Autowired
	private AccountService accountService;
	private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
	private static final DateTimeFormatter dateTileFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	@Autowired
	private JavaMailSender javaMailSender;
	
/**	// s'éxécute tous les deux secondes
	@Scheduled(fixedRate = 2000)
	public void scheduleTaskWhithFixedRate() {
		logger.info("Fixed Rate Tasks :: Execution Time - {}", dateTileFormatter.format(LocalDateTime.now()));

	}*/
/**	// s'éxécute de deux secondes entre la tache qui termine et le suivant qui commence
	@Scheduled(fixedDelay = 2000)
	public void scheduleTaskWhithFixedDelay() {
		logger.info("Fixed Delay Tasks :: Execution Time - {}", dateTileFormatter.format(LocalDateTime.now()));
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException ex) {
			logger.error("Run into an error {}", ex);
			throw new IllegalStateException(ex);
		}
		
	}*/
	
/**	//	le premier exécution de la tache sera retardée de 5 seconde 
	@Scheduled(fixedRate = 2000, initialDelay = 5000)
	public void scheduleTaskWhithInitialDelay() {
		logger.info("Fixed Rate Tasks with Initial Delay :: Execution Time - {}", dateTileFormatter.format(LocalDateTime.now()));
	}*/
	@Scheduled(cron = "0 * * * * ?")
	public void scheduleTaskWhithCronExpression() {
		logger.info("Cron Task :: Execution Time - {}", dateTileFormatter.format(LocalDateTime.now()));
//		AppUser user = this.accountService.findUserByUsername("admin");
//		user.setRestPasswordToken(null);
//		this.accountService.saveUser(user);
		this.accountService.findAllUser(10).forEach(user -> {
//			System.out.println(user.getUsername()+" : "+user.getEmail());
//			try {
//				this.sendEmail(user.getEmail());
//			} catch (UnsupportedEncodingException | MessagingException e) {
//				e.printStackTrace();
//			}
		});
	}
	
	private void sendEmail(String email) throws UnsupportedEncodingException, MessagingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		helper.setFrom("contact@yopmail.com", "cordialement waly");
		helper.setTo(email);
		String subject = "Voici le lien de reset password !!!";
		String content = "<p>Salut,</p>"
				+ "<p>vous avez un requéte dereset password.</p>"
				+"<p>Cliquer sur ce lien pour changer votre mot de passe : </p>"
				+"<p><b>Changer votre mot de passe</b></p>"
				+"<p>Ignor le mail si vous rappelé  de votre mot de pass !!!</p>";
		helper.setSubject(subject);
		helper.setText(content,true);
		javaMailSender.send(message);
	}

}
