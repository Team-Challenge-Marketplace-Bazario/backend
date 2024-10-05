package io.teamchallenge.project.bazario.helpers;

import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.entity.Verification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EMailHelper {

    private final String frontendUrl;
    private final JavaMailSender mailSender;

    public EMailHelper(@Value("${app.frontend-url}") String frontendUrl,
                       JavaMailSender mailSender) {
        this.frontendUrl = frontendUrl;
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(User user) {
        final var mailMessage = new SimpleMailMessage();

        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Email verification");
        mailMessage.setText(getMessageBody(user.getVerification()));

        mailSender.send(mailMessage);
    }

    private String getMessageBody(Verification verification) {
        final var link = String.format("%s/verify-email?token=%s", frontendUrl, verification.getToken());
        return String.format("Click the following link to verify your email address: %s", link);
    }
}
