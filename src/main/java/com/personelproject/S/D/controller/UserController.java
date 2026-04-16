package com.personelproject.S.D.controller;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.personelproject.S.D.model.Parcel;
import com.personelproject.S.D.model.User;
import com.personelproject.S.D.model.UserResetPasswordRequest;
import com.personelproject.S.D.service.EmailService;
import com.personelproject.S.D.service.ParcelService;
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
import org.springframework.http.HttpStatus;
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
    @Autowired
    private ParcelService parcelService;


    @GetMapping("/test-props")
    public ResponseEntity<?> testProps() {
        try {
            InputStream is = getClass().getResourceAsStream("/application.properties");
            if (is != null) {
                return ResponseEntity.ok("application.properties FOUND in JAR");
            } else {
                return ResponseEntity.status(500).body("application.properties NOT FOUND in JAR");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
    
    @GetMapping("users/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("users/id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @PostMapping(value = "/auth/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createUser(@RequestPart("user") String userJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        User user = mapper.readValue(userJson, User.class);

        User existingUser = userService.findUserByEmailAndCin(
                user.getEmail(),
                user.getCin());

        if (existingUser != null) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
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
                Map.of("user", savedUser, "code", code));
    }

    @PostMapping("/auth/confirmation/{email}/resend")
    public ResponseEntity<?> resendConfirmationCode(@PathVariable String email) {
        User user = userService.findUserByEmail(email);
        if (user == null) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }
        String code = emailService.sendConfirmationCode(email);
        return ResponseEntity.ok(Map.of("code", code));
    }

    @PostMapping("/auth/validate/{email}")
    public ResponseEntity<?> ValidateAccount(@PathVariable String email) {
        User user = userService.findUserByEmail(email);
        if (user == null) {
            return ResponseEntity
                    .badRequest().build();
        }
        user.setStatus("Activated");
        userService.updateUser(user.getId(), user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody UserResetPasswordRequest request) {
        User user = userService.findUserByEmailAndCin(request.getEmail(), request.getCin());

        if (user != null) {
            System.out.println(user);
            emailService.sendPasswordResetcode(request.getEmail(), user);

            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/user/update-password/{password}")
    public ResponseEntity<?> updateUserPassword(@PathVariable String password,
            @RequestBody UserResetPasswordRequest user) {

        User userr = userService.findUserByEmailAndCin(user.getEmail(), user.getCin());

        if (userr != null) {
            userr.setPassword(password);
            userService.updateUser(userr.getId(), userr);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

    @PutMapping(value = "users/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestPart("user") String userJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        User user = mapper.readValue(userJson, User.class);

        User existingUser = userService.findUserById(id);

        if (file != null && !file.isEmpty()) {

            if (existingUser.getPhotoId() != null) {
                photoService.deletePhoto(existingUser.getPhotoId());
            }

            String photoId = photoService.uploadPhoto(file);
            user.setPhotoId(photoId);

        } else {

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
                .body((org.springframework.core.io.Resource) new InputStreamResource(
                        photoService.getResource(file).getInputStream()));
    }

    @PutMapping("users/admin/block/{id}/{days}")
    public ResponseEntity<User> blockUser(@PathVariable String id, @PathVariable int days) {
        User user = userService.findUserById(id);
        if (user != null) {
            userService.blockUser(id);
            emailService.sendAccountBlockEmail(user.getEmail(), days);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("users/admin/unblock/{id}")
    public ResponseEntity<User> unblockUser(@PathVariable String id) {
        User user = userService.findUserById(id);
        if(user != null){
            userService.unblockUser(id);
            emailService.sendAccountUnblockEmail(user.getEmail());
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("users/admin/make-admin/{id}")
    public ResponseEntity<User> makeAdmin(@PathVariable String id) {
        User user = userService.makeAdmin(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("users/admin/make-user/{id}")
    public ResponseEntity<User> makeUser(@PathVariable String id) {
        User user = userService.makeUser(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("users/admin/make-transporter/{id}")
    public ResponseEntity<User> makeTransporter(@PathVariable String id) {
        User user = userService.makeTransporter(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("users/admin/remove-transporter/{id}")
    public ResponseEntity<User> removeTransporter(@PathVariable String id) {
        List<Parcel> parcels = parcelService.findByTransporterId(id);
        if(parcels.size() > 0){
            for (Parcel parcel : parcels) {
                parcel.setTransporterId(null);
                
            }
            
        }
        
        User user = userService.makeUser(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("users/transporters/all")
    public ResponseEntity<List<User>> getAllTransporters() {
        List<User> transporters = userService.findUsersByRole("Transporter");
        return ResponseEntity.ok(transporters);
    }

    @DeleteMapping("users/{id}/photo")
    public ResponseEntity<?> deleteUserPhoto(@PathVariable String id) {

        User user = userService.findUserById(id);

        if (user.getPhotoId() != null) {
            photoService.deletePhoto(user.getPhotoId());
            userService.removeUserPhoto(id);
        }

        return ResponseEntity.ok().build();
    }



}
