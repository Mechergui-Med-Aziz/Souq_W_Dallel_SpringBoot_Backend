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


public void sendPasswordResetcode(String toEmail, User u) {
    String newCode = generateRandomCode();
    
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("souq.w.dallel@gmail.com");
    message.setTo(toEmail);
    message.setSubject("Réinitialisation de votre mot de passe");
    
    message.setText(
        "Bonjour " + u.getFirstname() + ",\n\n" +
        "Nous avons bien reçu votre demande de réinitialisation de mot de passe.\n"+
        "Voici le code d'accés pour reinitialier votre mot de passe : " + newCode + "\n\n" +
        "Si vous n'êtes pas à l'origine de cette demande, veuillez contacter notre support technique immédiatement.\n\n" +
        "Cordialement,\n" +
        "L'équipe S&D Auction\n" +
        "souq.w.dallel@gmail.com"
    );
    
    emailSender.send(message);
    }

    public void sendAuctionWinEmail(User u, String auctionTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("souq.w.dallel@gmail.com");
        message.setTo(u.getEmail());
        message.setSubject("Félicitations ! Vous avez gagné l'enchère");
    
        message.setText(
            "Bonjour " + u.getFirstname() + ",\n\n" +
            "Félicitations ! 🎉\n\n" +
            "Vous avez remporté l'enchère pour : \"" + auctionTitle + "\".\n\n" +
            "Nous vous invitons à effectuer le paiement dans un délai de 24 heures afin de finaliser votre achat.\n\n" +
            "⚠️ Passé ce délai, l'enchère sera automatiquement attribuée au prochain enchérisseur.\n\n" +
            "Si vous avez des questions, n'hésitez pas à contacter notre support.\n\n" +
            "Cordialement,\n" +
            "L'équipe S&D Auction\n" +
            "souq.w.dallel@gmail.com"
        );
    
        emailSender.send(message);
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

public void sendAccountBlockEmail(String toEmail, int days) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("souq.w.dallel@gmail.com");
    message.setTo(toEmail);
    message.setSubject("Notification de suspension temporaire de votre compte – S&D Auction");

    User u = userService.findUserByEmail(toEmail);

    message.setText(
        "Bonjour " + u.getFirstname() + ",\n\n" +
        "Nous vous informons que votre compte sur la plateforme S&D Auction a été temporairement suspendu.\n\n" +
        "Cette suspension est due à une violation des règles d’utilisation de la plateforme.\n\n" +
        "Durée de la suspension : " + days + " jour(s).\n\n" +
        "Pendant cette période, vous ne pourrez pas participer aux enchères ni effectuer d’actions sur votre compte.\n\n" +
        "Nous vous invitons à consulter les règles de la plateforme afin d’éviter toute nouvelle infraction.\n\n" +
        "Si vous pensez qu’il s’agit d’une erreur ou si vous souhaitez obtenir plus d’informations, veuillez contacter notre support.\n\n" +
        "Cordialement,\n" +
        "L’équipe S&D Auction\n" +
        "souq.w.dallel@gmail.com"
    );

    emailSender.send(message);
}

public void sendAccountUnblockEmail(String toEmail) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("souq.w.dallel@gmail.com");
    message.setTo(toEmail);
    message.setSubject("Votre compte a été réactivé – S&D Auction");

    User u = userService.findUserByEmail(toEmail);

    message.setText(
        "Bonjour " + u.getFirstname() + ",\n\n" +
        "Nous vous informons que la suspension temporaire appliquée à votre compte S&D Auction a pris fin.\n\n" +
        "Votre compte est désormais de nouveau actif et vous pouvez accéder à toutes les fonctionnalités de la plateforme, " +
        "y compris participer aux enchères et gérer votre profil.\n\n" +
        "Nous vous rappelons de respecter les règles d’utilisation de la plateforme afin d’éviter toute nouvelle suspension.\n\n" +
        "Si vous avez des questions ou besoin d’assistance, notre équipe de support reste à votre disposition.\n\n" +
        "Cordialement,\n" +
        "L’équipe S&D Auction\n" +
        "souq.w.dallel@gmail.com"
    );

    emailSender.send(message);
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
