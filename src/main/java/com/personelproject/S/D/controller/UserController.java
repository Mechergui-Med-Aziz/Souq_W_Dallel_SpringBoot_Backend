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
import com.personelproject.S.D.repository.UserRepository;

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
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private org.springframework.core.env.Environment environment;

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

    @GetMapping("/test-mongo-url")
    public ResponseEntity<?> testMongoUrl() {
        String mongoUri = environment.getProperty("spring.data.mongodb.uri");
        return ResponseEntity.ok("MongoDB URI: " + mongoUri);
    }

    @GetMapping("/test-mongo-ping")
    public ResponseEntity<?> testMongoPing() {
        try {
            long count = userRepository.count();
            return ResponseEntity.ok(Map.of(
                "status", "MongoDB connected successfully",
                "userCount", count
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "status", "MongoDB connection failed",
                "error", e.getMessage(),
                "errorType", e.getClass().getSimpleName()
            ));
        }
    }

    @PostMapping(value = "/auth/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createUser(@RequestPart("user") String userJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            System.out.println("=== REGISTER REQUEST RECEIVED ===");
            System.out.println("User JSON: " + userJson);
            
            ObjectMapper mapper = new ObjectMapper();
            User user = mapper.readValue(userJson, User.class);
            
            System.out.println("Email: " + user.getEmail());
            System.out.println("CIN: " + user.getCin());

            // Check if user already exists
            User existingUser = userService.findUserByEmailAndCin(
                    user.getEmail(),
                    user.getCin());

            if (existingUser != null) {
                System.out.println("User already exists with email: " + user.getEmail());
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "User with this email or CIN already exists"));
            }

            // Handle photo upload
            if (file != null && !file.isEmpty()) {
                System.out.println("Photo file received: " + file.getOriginalFilename());
                String photoId = photoService.uploadPhoto(file);
                user.setPhotoId(photoId);
            }

            user.setRole("User");
            user.setStatus("Waiting for validation");

            // Save user
            User savedUser = userService.saveUser(user);
            System.out.println("User saved with ID: " + savedUser.getId());
            
            // Send confirmation email
            String code = emailService.sendConfirmationCode(savedUser.getEmail());
            System.out.println("Confirmation code sent to: " + savedUser.getEmail());

            return ResponseEntity.ok(Map.of(
                "user", savedUser, 
                "code", code,
                "message", "Registration successful. Please check your email for confirmation code."
            ));
            
        } catch (Exception e) {
            System.err.println("=== REGISTER ERROR ===");
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Registration failed",
                        "message", e.getMessage(),
                        "type", e.getClass().getSimpleName()
                    ));
        }
    }

    @PostMapping("/auth/confirmation/{email}/resend")
    public ResponseEntity<?> resendConfirmationCode(@PathVariable String email) {
        try {
            User user = userService.findUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            String code = emailService.sendConfirmationCode(email);
            return ResponseEntity.ok(Map.of("code", code, "message", "Code resent successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/auth/validate/{email}")
    public ResponseEntity<?> ValidateAccount(@PathVariable String email) {
        try {
            User user = userService.findUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            user.setStatus("Activated");
            userService.updateUser(user.getId(), user);
            return ResponseEntity.ok(Map.of("message", "Account validated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody UserResetPasswordRequest request) {
        try {
            User user = userService.findUserByEmailAndCin(request.getEmail(), request.getCin());
            if (user != null) {
                emailService.sendPasswordResetcode(request.getEmail(), user);
                return ResponseEntity.ok(Map.of("message", "Reset code sent to your email"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "User not found with provided email and CIN"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/user/update-password/{password}")
    public ResponseEntity<?> updateUserPassword(@PathVariable String password,
            @RequestBody UserResetPasswordRequest user) {
        try {
            User userr = userService.findUserByEmailAndCin(user.getEmail(), user.getCin());
            if (userr != null) {
                userr.setPassword(password);
                userService.updateUser(userr.getId(), userr);
                return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
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
        if (user != null) {
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
        if (parcels.size() > 0) {
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
