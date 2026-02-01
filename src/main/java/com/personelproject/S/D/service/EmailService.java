package com.personelproject.S.D.service;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.personelproject.S.D.model.User;
@Service
public class EmailService {
     @Autowired
private JavaMailSender emailSender;
@Autowired
private UserService userService;


public void sendPasswordResetEmail(String toEmail, User u) {
    String newPassword = generateRandomPassword();
    
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("souq.w.dallel@gmail.com");
    message.setTo(toEmail);
    message.setSubject("Réinitialisation de votre mot de passe");
    
    message.setText(
        "Bonjour " + u.getFirstname() + ",\n\n" +
        "Nous avons bien reçu votre demande de réinitialisation de mot de passe.\n"+
        "Voici votre nouveau mot de passe temporaire : " + newPassword + "\n\n" +
        "Pour des raisons de sécurité, nous vous recommandons de le modifier dès votre prochaine connexion.\n\n" +
        "Si vous n'êtes pas à l'origine de cette demande, veuillez contacter notre support technique immédiatement.\n\n" +
        "Cordialement,\n" +
        "L'équipe S&D Auction\n" +
        "souq.w.dallel@gmail.com"
    );
    
    emailSender.send(message);
    
    u.setPassword(newPassword);
    userService.updateUser(u.getId(), u);
}

public String sendConfirmationCode(String toEmail) {
    String code = generateRandomCode();

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("souq.w.dallel@gmail.com");
    message.setTo(toEmail);
    message.setSubject("Code de validation – S&D Auction");

    User u = userService.findUserByEmail(toEmail);


    message.setText(
        "Bonjour " + u.getFirstname() + ",\n\n" +
        "Merci d’avoir effectué une demande de création d'un compte chez nous.\n\n" +
        "Voici votre code de validation : " + code + "\n\n" +
        "Ce code est strictement personnel et valable pour une durée limitée.\n" +
        "Veuillez le saisir dans l’application afin de finaliser la création de votre compte.\n\n" +
        "Si vous n’êtes pas à l’origine de cette demande, veuillez ignorer cet email ou contacter notre support technique.\n\n" +
        "Cordialement,\n" +
        "L’équipe S&D Auction\n" +
        "souq.w.dallel@gmail.com"
    );

    emailSender.send(message);
    return code;
}


public void sendActivationAccountEmail(String toEmail) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("souq.w.dallel@gmail.com");
    message.setTo(toEmail);
    message.setSubject("Activation de votre compte – S&D Auction");

    User u = userService.findUserByEmail(toEmail);

    message.setText(
        "Bonjour " + u.getFirstname() + ",\n\n" +
        "Nous sommes heureux de vous informer que votre compte sur la plateforme S&D auction a été activé avec succès.\n\n" +
        "Vous pouvez désormais vous connecter et accéder aux fonctionnalités de la plateforme.\n\n" +
        "Si vous rencontrez des difficultés lors de la connexion ou si vous avez des questions, n’hésitez pas à contacter notre support technique.\n\n" +
        "Cordialement,\n" +
        "L’équipe S&D Auction\n" +
        "souq.w.dallel@gmail.com"
    );

    emailSender.send(message);
}



private String generateRandomPassword() {
    
    int length = 8;
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-*$#@!";
    Random rand = new Random();
    StringBuilder password = new StringBuilder();
    for (int i = 0; i < length; i++) {
        int randomIndex = rand.nextInt(chars.length());
        password.append(chars.charAt(randomIndex));
    }
    return password.toString();
}

private String generateRandomCode() {
    
    int length = 6;
    String chars = "0123456789";
    Random rand = new Random();
    StringBuilder password = new StringBuilder();
    for (int i = 0; i < length; i++) {
        int randomIndex = rand.nextInt(chars.length());
        password.append(chars.charAt(randomIndex));
    }
    return password.toString();
}

}
