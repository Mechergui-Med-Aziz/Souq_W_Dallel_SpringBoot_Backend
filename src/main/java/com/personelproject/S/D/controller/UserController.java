package com.personelproject.S.D.controller;

// Removed redundant import
import java.util.HashMap;
import java.util.Map;

// Removed redundant import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.personelproject.S.D.model.User;
import com.personelproject.S.D.model.UserResetPasswordRequest;
import com.personelproject.S.D.service.EmailService;
import com.personelproject.S.D.service.PhotoService;
import com.personelproject.S.D.service.UserService;

import tools.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;



@RestController
@RequestMapping("api")

public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PhotoService photoService;


    @GetMapping("users/id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

   @PostMapping(value = "/auth/register",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createUser(@RequestPart("user") String userJson,@RequestPart(value = "file", required = false) MultipartFile file) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        User user = mapper.readValue(userJson, User.class);

        User existingUser = userService.findUserByEmailAndCin(
                user.getEmail(),
                user.getCin()
        );

        if (existingUser != null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message",
                            "Un utilisateur avec cet email ou ce CIN existe déjà !"));
        }

        if (file != null && !file.isEmpty()) {
            String photoId = photoService.uploadPhoto(file);
            user.setPhotoId(photoId);
        }

        user.setRole("User");
        user.setStatus("Waiting for validation");

        User savedUser = userService.saveUser(user);
        String code = emailService.sendConfirmationCode(savedUser.getEmail());

        return ResponseEntity.ok(
                Map.of("user", savedUser, "code", code)
        );
    }


    @PostMapping("/auth/confirmation/{email}/resend")
    public ResponseEntity<?> resendConfirmationCode(@PathVariable String email) {
        User user = userService.findUserByEmail(email);
        if (user == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Utilisateur non trouvé !"));
        }
        String code =emailService.sendConfirmationCode(email);
        return ResponseEntity.ok(Map.of("code",code));
    }

    @PostMapping("/reset-password")
public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody UserResetPasswordRequest request) {
    User user = userService.findUserByEmailAndCin( request.getEmail(),request.getCin());

    if (user != null) {
        System.out.println(user);
        emailService.sendPasswordResetEmail(request.getEmail(), user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Un email de réinitialisation de mot de passe a été envoyé, Veuillez vérifier votre boîte de réception.");
        
        return ResponseEntity.ok(response);
    }

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("success", false);
    errorResponse.put("message", "Aucun utilisateur trouvé avec ce cin et cet email.");
    
    return ResponseEntity.badRequest().body(errorResponse);
}
    
    


    @PutMapping(value = "users/update/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestPart("user") String userJson,@RequestPart(value = "file", required = false) MultipartFile file)throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        User user = mapper.readValue(userJson, User.class);
        
        User existingUser = userService.findUserById(id);

        // 🖼️ Si une nouvelle photo est envoyée
        if (file != null && !file.isEmpty()) {

            // supprimer l’ancienne photo
            if (existingUser.getPhotoId() != null) {
                photoService.deletePhoto(existingUser.getPhotoId());
            }

            // upload nouvelle photo
            String photoId = photoService.uploadPhoto(file);
            user.setPhotoId(photoId);

        } else {
            // garder l’ancienne photo
            user.setPhotoId(existingUser.getPhotoId());
        }

        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    
    @GetMapping("users/{id}/photo")
    public ResponseEntity<?> getUserPhoto(@PathVariable String id) throws Exception {

        User user = userService.findUserById(id);

        if (user.getPhotoId() == null) {
            return ResponseEntity.notFound().build();
        }

        GridFSFile file = photoService.getPhoto(user.getPhotoId());

        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        file.getMetadata().getString("_contentType")))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getFilename() + "\"")
                .body((org.springframework.core.io.Resource)
                        new InputStreamResource(
                                photoService.getResource(file).getInputStream()
                        ));
    }



    @DeleteMapping("users/{id}/photo")
    public ResponseEntity<?> deleteUserPhoto(@PathVariable String id) {
    
        User user = userService.findUserById(id);
    
        if (user.getPhotoId() != null) {
            photoService.deletePhoto(user.getPhotoId());
            userService.removeUserPhoto(id);
        }
    
        return ResponseEntity.ok(Map.of("message", "Photo supprimée"));
    }
    

}
