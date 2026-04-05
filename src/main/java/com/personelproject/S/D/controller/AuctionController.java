package com.personelproject.S.D.controller;

import com.personelproject.S.D.service.EmailService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.personelproject.S.D.model.Auction;
import com.personelproject.S.D.model.AuctionsBidsDeposit;
import com.personelproject.S.D.model.Notification;
import com.personelproject.S.D.model.User;
import com.personelproject.S.D.service.AuctionService;
import com.personelproject.S.D.service.AuctionsBidsDepositService;
import com.personelproject.S.D.service.NotificationService;
import com.personelproject.S.D.service.PhotoService;
import com.personelproject.S.D.service.UserService;

import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("api/auctions")
public class AuctionController {
    @Autowired
    private EmailService emailService;
    @Autowired
    private AuctionService auctionService;
    @Autowired
    private PhotoService photoService;
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AuctionsBidsDepositService auctionsBidsDepositService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAuction(@RequestPart("auction") String auctionjson,
            @RequestPart("files") List<MultipartFile> files) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        Auction auction = mapper.readValue(auctionjson, Auction.class);
        List<String> photoIds = new ArrayList<>();
        for (MultipartFile file : files) {
            photoIds.add(photoService.uploadPhoto(file));
        }

        auction.setPhotoId(photoIds);
        auction.setStatus("waiting for payment");

        Auction createdAuction = auctionService.saveAuction(auction);

        // Notify all admins about new auction pending approval
        List<User> admins = userService.findUsersByRole("ADMIN");
        for (User admin : admins) {
            Notification notification = Notification.builder()
                    .userId(admin.getId())
                    .auctionId(createdAuction.getId())
                    .message("Nouvelle enchère en attente d'approbation: " + createdAuction.getTitle())
                    .type(Notification.Type.AUCTION_PENDING)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationService.saveNotification(notification);
        }

        return ResponseEntity.ok(createdAuction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Auction> getAuctionById(@PathVariable String id) {
        Auction auction = auctionService.findAuctionById(id);
        return ResponseEntity.ok(auction);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Auction> updateAuction(@PathVariable String id,
            @RequestPart("auction") String auctionjson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "removedPhotoIds", required = false) String removedPhotoIdsJson) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        Auction incomingAuction = mapper.readValue(auctionjson, Auction.class);

        Auction existingAuction = auctionService.findAuctionById(id);

        // Only update fields that are allowed to change
        existingAuction.setTitle(incomingAuction.getTitle());
        existingAuction.setDescription(incomingAuction.getDescription());
        existingAuction.setStartingPrice(incomingAuction.getStartingPrice());
        existingAuction.setCategory(incomingAuction.getCategory());
        existingAuction.setExpireDate(incomingAuction.getExpireDate());

        List<String> removedPhotoIds = new ArrayList<>();
        if (removedPhotoIdsJson != null && !removedPhotoIdsJson.isEmpty()) {
            removedPhotoIds = mapper.readValue(removedPhotoIdsJson,
                    mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        }

        // Delete removed photos
        if (!removedPhotoIds.isEmpty()) {
            for (String photoId : removedPhotoIds) {
                try {
                    photoService.deletePhoto(photoId);
                } catch (Exception e) {
                    System.out.println("Error deleting photo: " + e.getMessage());
                }
            }
        }

        // Keep existing photos that weren't removed
        List<String> updatedPhotoIds = new ArrayList<>();
        if (existingAuction.getPhotoId() != null) {
            for (String photoId : existingAuction.getPhotoId()) {
                if (!removedPhotoIds.contains(photoId)) {
                    updatedPhotoIds.add(photoId);
                }
            }
        }

        // Upload new photos
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                updatedPhotoIds.add(photoService.uploadPhoto(file));
            }
        }

        existingAuction.setPhotoId(updatedPhotoIds);

        Auction updatedAuction = auctionService.updateAuction(existingAuction);
        return ResponseEntity.ok(updatedAuction);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAuction(@PathVariable String id) {
        auctionService.deleteAuction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public List<Auction> getAllAuctions() {
        return auctionService.findAllAuctions();
    }

    @GetMapping("/auctions/{status}")
    public List<Auction> getAuctionsByStatus(@PathVariable String status) {
        return auctionService.findAuctionByStatus(status);
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<?> getAuctionsBySellerId(@PathVariable String sellerId) {
        List<Auction> auctions = auctionService.findAuctionsBySellerId(sellerId);
        return ResponseEntity.ok(auctions);
    }

    @GetMapping("/{id}/photos/{photoId}")
    public ResponseEntity<?> getAuctionPhoto(@PathVariable String id, @PathVariable String photoId) throws Exception {

        Auction auction = auctionService.findAuctionById(id);

        if (auction == null) {
            return ResponseEntity.notFound().build();
        }

        GridFSFile file = photoService.getPhoto(photoId);

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

    @PutMapping("bid/add/{idAuction}/{idBidder}/{bidAmount}")
    public ResponseEntity<?> placeBid(@PathVariable String idAuction, @PathVariable String idBidder,
            @PathVariable Double bidAmount) {

        if (userService.findUserById(idBidder) == null) {
            return ResponseEntity.badRequest().body("Bidder not found with id: " + idBidder);
        }

        if (auctionService.findAuctionById(idAuction) == null) {
            return ResponseEntity.badRequest().body("Auction not found with id: " + idAuction);
        }

        try {
            Auction updatedAuction = auctionService.placeBid(idAuction, idBidder, bidAmount);

            Notification notif = notificationService.save(updatedAuction.getSellerId(), updatedAuction.getId());

            // Check if deposit exists for this auction and type "bids"
            AuctionsBidsDeposit abd = auctionsBidsDepositService.findDepositsByAuctionIdAndType(idAuction, "bids");
            if (abd != null) {
                abd.setAmount(abd.getAmount() + 1D);
                auctionsBidsDepositService.updateDeposit(abd);
            } else {
                abd = new AuctionsBidsDeposit();
                abd.setType(AuctionsBidsDeposit.Type.BIDS);
                abd.setAuctionId(idAuction);
                abd.setAmount(1D);
                auctionsBidsDepositService.saveDeposit(abd); // Save new deposit
            }

            return ResponseEntity.ok(Map.of("auction", updatedAuction, "notification", notif));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/auction/reviews/{auctionId}")
    public ResponseEntity<?> getReviews(@PathVariable String auctionId) {
        Auction auction = auctionService.findAuctionById(auctionId);
        if (auction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(auctionService.getReviews(auctionId));

    }

    @PostMapping("auction/addReview/{auctionId}/{reviewerId}/{review}")
    public ResponseEntity<?> addReview(@PathVariable String auctionId,
            @PathVariable String reviewerId,
            @PathVariable String review) {
        try {
            Auction auction = auctionService.findAuctionById(auctionId);
            if (auction == null) {
                return ResponseEntity.notFound().build();
            }
            boolean success = auctionService.addReview(auctionId, reviewerId, review);
            if (success) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.badRequest().body("Failed to add review");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("auction/updateReview/{auctionId}/{reviewerId}/{oldReview}/{newReview}")
    public ResponseEntity<?> updateReview(@PathVariable String auctionId,
            @PathVariable String reviewerId,
            @PathVariable String oldReview,
            @PathVariable String newReview) {
        try {
            Auction auction = auctionService.findAuctionById(auctionId);
            if (auction == null) {
                return ResponseEntity.notFound().build();
            }
            boolean success = auctionService.updateReview(auctionId, reviewerId, oldReview, newReview);
            if (success) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.badRequest().body("Failed to update review");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("auction/deleteReview/{auctionId}/{reviewerId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable String auctionId,
            @PathVariable String reviewerId,
            @RequestParam String review) {
        try {
            Auction auction = auctionService.findAuctionById(auctionId);
            if (auction == null) {
                return ResponseEntity.notFound().build();
            }
            boolean success = auctionService.deleteReview(auctionId, reviewerId, review);
            if (success) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.badRequest().body("Failed to delete review");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("auction/{auctionId}/{adminId}/{status}")
    public ResponseEntity<?> denyApproveAuction(@PathVariable String auctionId, @PathVariable String adminId,
            @PathVariable String status) {
        Auction auction = auctionService.findAuctionById(auctionId);
        if (auction != null) {
            auction.setAdminId(adminId);
            auction.setStatus(status);
            auctionService.updateAuction(auction);
            return ResponseEntity.ok(auction);
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/auction/ended/{auctionId}")
    public ResponseEntity<?> wonAuction(@PathVariable String auctionId) {
        Auction auction = auctionService.findAuctionById(auctionId);
        if (auction == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if already processed
        if ("ended".equals(auction.getStatus())) {
            System.out.println("Auction already processed, skipping");
            return ResponseEntity.ok().build();
        }

        String winnerId = auction.getBidders().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (winnerId != null) {
            User winner = userService.findUserById(winnerId);

            // Update auction status and save to database
            auction.setStatus("ended");
            auctionService.updateAuction(auction);

            // Send email
            emailService.sendAuctionWinEmail(winner, auction.getTitle());

            // Create notification for winner
            Notification notification = Notification.builder()
                    .userId(winnerId)
                    .auctionId(auctionId)
                    .message("Félicitations ! Vous avez gagné l'enchère : " + auction.getTitle())
                    .type(Notification.Type.AUCTION_WON)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Save notification to database
            notificationService.saveNotification(notification);

            System.out.println("Winner notification created for user: " + winnerId);
            System.out.println("Auction status set to ended, payment not yet made");
        } else {
            System.out.println("No winner found for auction: " + auctionId);
        }

        return ResponseEntity.ok().build();
    }

}