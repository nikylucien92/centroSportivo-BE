package it.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender javaMailSender;

	@Async
	public void sendEmail(String to, String subject, String body) {

		try {
			MimeMessage message = javaMailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(body, true);

			javaMailSender.send(message);

		} catch (MessagingException e) {
			throw new RuntimeException("Errore durante l'invio dell'email a " + to, e);
		}
	}
}
