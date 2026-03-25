package com.personelproject.S.D.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.personelproject.S.D.model.Notification;
import com.personelproject.S.D.model.Parcel;
import com.personelproject.S.D.model.User;
import com.personelproject.S.D.service.NotificationService;
import com.personelproject.S.D.service.ParcelService;
import com.personelproject.S.D.service.UserService;

@RestController
@RequestMapping("/api/parcels")
public class ParcelController {

    @Autowired
    private ParcelService parcelService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllParcels() {
        try {
            return ResponseEntity.ok(parcelService.findAllParcels());
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getParcelById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(parcelService.findParcelById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteParcel(@PathVariable String id) {
        parcelService.deleteParcel(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateParcel(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        Parcel existingParcel = parcelService.findParcelById(id);
        if (existingParcel == null) {
            return ResponseEntity.notFound().build();
        }

        // Only update fields that are provided in the request
        if (updates.containsKey("transporterId")) {
            existingParcel.setTransporterId((String) updates.get("transporterId"));
        }
        if (updates.containsKey("pickUpAdress")) {
            existingParcel.setPickUpAdress((String) updates.get("pickUpAdress"));
        }
        if (updates.containsKey("destinationAdress")) {
            existingParcel.setDestinationAdress((String) updates.get("destinationAdress"));
        }
        if (updates.containsKey("adminId")) {
            existingParcel.setAdminId((String) updates.get("adminId"));
        }
        if (updates.containsKey("isValid")) {
            existingParcel.setIsValid((Boolean) updates.get("isValid"));
        }
        if (updates.containsKey("unvalidDescription")) {
            existingParcel.setUnvalidDescription((String) updates.get("unvalidDescription"));
        }

        return ResponseEntity.ok(parcelService.updateParcel(existingParcel));
    }

    @PutMapping("/validate/{id}/{isValid}")
    public ResponseEntity<?> updateQualityCheck(@PathVariable String id,
            @PathVariable boolean isValid,
            @RequestParam(required = false, defaultValue = "") String description) {
        Parcel parcel = parcelService.findParcelById(id);
        if (parcel == null) {
            return ResponseEntity.notFound().build();
        }
        parcel.setIsValid(isValid);
        if (!isValid && description != null && !description.isEmpty()) {
            parcel.setUnvalidDescription(description);
            
            // Notify admin about invalid parcel
            User admin = userService.findUserById(parcel.getAdminId());
            if (admin != null) {
                Notification notification = Notification.builder()
                        .userId(admin.getId())
                        .auctionId(parcel.getAuctionId())
                        .message("Colis non conforme signalé pour l'enchère #" + parcel.getAuctionId())
                        .type(Notification.Type.PARCEL_INVALID)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationService.saveNotification(notification);
            }
        }
        return ResponseEntity.ok(parcelService.updateParcel(parcel));
    }

    @PutMapping("/delivred/{id}")
    public ResponseEntity<?> deliveredParcel(@PathVariable String id) {
        Parcel parcel = parcelService.findParcelById(id);
        if (parcel != null) {
            parcel.setDelivred(true);
            parcel.setDelivredAt(java.sql.Date.valueOf(LocalDateTime.now().toLocalDate()));
            return ResponseEntity.ok(parcelService.updateParcel(parcel));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/admin/{adminId}")
    public ResponseEntity<?> getParcelsByAdmin(@PathVariable String adminId) {
        try {
            List<Parcel> parcels = parcelService.findByAdminId(adminId);
            return ResponseEntity.ok(parcels);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/transporter/{transporterId}")
    public ResponseEntity<?> getParcelsByTransporter(@PathVariable String transporterId) {
        try {
            List<Parcel> parcels = parcelService.findByTransporterId(transporterId);
            return ResponseEntity.ok(parcels);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<?> getParcelsByBuyer(@PathVariable String buyerId) {
        try {
            List<Parcel> parcels = parcelService.findByBuyerId(buyerId);
            return ResponseEntity.ok(parcels);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
}