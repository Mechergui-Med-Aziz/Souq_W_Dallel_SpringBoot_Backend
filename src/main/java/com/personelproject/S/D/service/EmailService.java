package com.personelproject.S.D.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.personelproject.S.D.model.User;

@Service
public class EmailService {

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    @Autowired
    private UserService userService;

    private final String SENDER_EMAIL = "souq.w.dallel@gmail.com";

    private void sendEmail(String toEmail, String subject, String content) {

        String url = "https://api.brevo.com/v3/smtp/email";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("email", SENDER_EMAIL);

        Map<String, String> to = new HashMap<>();
        to.put("email", toEmail);

        body.put("sender", sender);
        body.put("to", new Object[]{to});
        body.put("subject", subject);

        // ⚠️ convertir ton texte en HTML propre
        String htmlContent = content.replace("\n", "<br>");

        body.put("htmlContent", htmlContent);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        restTemplate.postForEntity(url, request, String.class);
    }

    public void sendPasswordResetcode(String toEmail, User u) {
        String newCode = generateRandomCode();

        String content =
            "Bonjour " + u.getFirstname() + ",\n\n" +
            "Nous avons bien reçu votre demande de réinitialisation de mot de passe.\n" +
            "Voici le code d'accès pour réinitialiser votre mot de passe : " + newCode + "\n\n" +
            "Si vous n'êtes pas à l'origine de cette demande, veuillez contacter notre support technique immédiatement.\n\n" +
            "Cordialement,\n" +
            "L'équipe S&D Auction\n" +
            "souq.w.dallel@gmail.com";

        sendEmail(toEmail, "Réinitialisation de votre mot de passe", content);
    }

    public void sendAuctionWinEmail(User u, String auctionTitle) {

        String content =
            "Bonjour " + u.getFirstname() + ",\n\n" +
            "Félicitations ! 🎉\n\n" +
            "Vous avez remporté l'enchère pour : \"" + auctionTitle + "\".\n\n" +
            "Nous vous invitons à effectuer le paiement dans un délai de 24 heures afin de finaliser votre achat.\n\n" +
            "⚠️ Passé ce délai, l'enchère sera automatiquement attribuée au prochain enchérisseur.\n\n" +
            "Si vous avez des questions, n'hésitez pas à contacter notre support.\n\n" +
            "Cordialement,\n" +
            "L'équipe S&D Auction\n" +
            "souq.w.dallel@gmail.com";

        sendEmail(u.getEmail(), "Félicitations ! Vous avez gagné l'enchère", content);
    }

    public String sendConfirmationCode(String toEmail) {
        String code = generateRandomCode();

        User u = userService.findUserByEmail(toEmail);

        String content =
            "Bonjour " + u.getFirstname() + ",\n\n" +
            "Merci d’avoir effectué une demande de création d'un compte chez nous.\n\n" +
            "Voici votre code de validation : " + code + "\n\n" +
            "Ce code est strictement personnel et valable pour une durée limitée.\n" +
            "Veuillez le saisir dans l’application afin de finaliser la création de votre compte.\n\n" +
            "Si vous n’êtes pas à l’origine de cette demande, veuillez ignorer cet email ou contacter notre support technique.\n\n" +
            "Cordialement,\n" +
            "L’équipe S&D Auction\n" +
            "souq.w.dallel@gmail.com";

        sendEmail(toEmail, "Code de validation – S&D Auction", content);

        return code;
    }

    public void sendActivationAccountEmail(String toEmail) {

        User u = userService.findUserByEmail(toEmail);

        String content =
            "Bonjour " + u.getFirstname() + ",\n\n" +
            "Nous sommes heureux de vous informer que votre compte sur la plateforme S&D auction a été activé avec succès.\n\n" +
            "Vous pouvez désormais vous connecter et accéder aux fonctionnalités de la plateforme.\n\n" +
            "Si vous rencontrez des difficultés lors de la connexion ou si vous avez des questions, n’hésitez pas à contacter notre support technique.\n\n" +
            "Cordialement,\n" +
            "L’équipe S&D Auction\n" +
            "souq.w.dallel@gmail.com";

        sendEmail(toEmail, "Activation de votre compte – S&D Auction", content);
    }

    public void sendAccountBlockEmail(String toEmail, int days) {

        User u = userService.findUserByEmail(toEmail);

        String content =
            "Bonjour " + u.getFirstname() + ",\n\n" +
            "Nous vous informons que votre compte sur la plateforme S&D Auction a été temporairement suspendu.\n\n" +
            "Cette suspension est due à une violation des règles d’utilisation de la plateforme.\n\n" +
            "Durée de la suspension : " + days + " jour(s).\n\n" +
            "Pendant cette période, vous ne pourrez pas participer aux enchères ni effectuer d’actions sur votre compte.\n\n" +
            "Nous vous invitons à consulter les règles de la plateforme afin d’éviter toute nouvelle infraction.\n\n" +
            "Si vous pensez qu’il s’agit d’une erreur ou si vous souhaitez obtenir plus d’informations, veuillez contacter notre support.\n\n" +
            "Cordialement,\n" +
            "L’équipe S&D Auction\n" +
            "souq.w.dallel@gmail.com";

        sendEmail(toEmail, "Notification de suspension temporaire", content);
    }

    public void sendAccountUnblockEmail(String toEmail) {

        User u = userService.findUserByEmail(toEmail);

        String content =
            "Bonjour " + u.getFirstname() + ",\n\n" +
            "Nous vous informons que la suspension temporaire appliquée à votre compte S&D Auction a pris fin.\n\n" +
            "Votre compte est désormais de nouveau actif.\n\n" +
            "Cordialement,\n" +
            "L’équipe S&D Auction\n" +
            "souq.w.dallel@gmail.com";

        sendEmail(toEmail, "Compte réactivé", content);
    }

    private String generateRandomCode() {
        int length = 6;
        String chars = "0123456789";
        Random rand = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(rand.nextInt(chars.length())));
        }

        return code.toString();
    }
}