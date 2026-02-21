package com.personelproject.S.D.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.personelproject.S.D.model.Auction;
import com.personelproject.S.D.model.Notification;
import com.personelproject.S.D.service.AuctionService;
import com.personelproject.S.D.service.NotificationService;
import com.personelproject.S.D.service.PhotoService;
import com.personelproject.S.D.service.UserService;

import tools.jackson.databind.ObjectMapper;




@RestController
@RequestMapping("api/auctions")
public class AuctionController {
    @Autowired
    private AuctionService auctionService;
    @Autowired
    private PhotoService photoService;
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationService notificationService;


    @PostMapping(value = "/create",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAuction(@RequestPart("auction") String auctionjson,@RequestPart("files") List<MultipartFile> files) throws Exception {

            ObjectMapper mapper = new ObjectMapper();
            Auction auction = mapper.readValue(auctionjson, Auction.class);
        List<String> photoIds = new ArrayList<>();
        for (MultipartFile file : files) {
            photoIds.add(photoService.uploadPhoto(file));
        }

        auction.setPhotoId(photoIds);

        Auction createdAuction = auctionService.saveAuction(auction);
        return ResponseEntity.ok(createdAuction);
    }


     @GetMapping("/{id}")
    public ResponseEntity<Auction> getAuctionById(@PathVariable String id) {
        Auction auction = auctionService.findAuctionById(id);
        return ResponseEntity.ok(auction);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Auction> updateAuction(@PathVariable String id, @RequestBody Auction auction) {
        Auction updatedAuction = auctionService.updateAuction(auction);
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

     @GetMapping("/seller/{sellerId}")
     public ResponseEntity<?> getAuctionsBySellerId(@PathVariable String sellerId) {
            List<Auction> auctions = auctionService.findAuctionsBySellerId(sellerId);
            return ResponseEntity.ok(auctions);
     }

     @GetMapping("/{id}/photos/{photoId}")
    public ResponseEntity<?> getAuctionPhoto(@PathVariable String id,@PathVariable String photoId) throws Exception {

        Auction auction = auctionService.findAuctionById(id);

        if (auction== null) {
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
                .body((org.springframework.core.io.Resource)
                        new InputStreamResource(
                                photoService.getResource(file).getInputStream()
                        ));
    }

        @PutMapping("bid/add/{idAuction}/{idBidder}/{bidAmount}")
        public ResponseEntity<?> placeBid(@PathVariable String idAuction,@PathVariable String idBidder,@PathVariable Double bidAmount) {

        if (userService.findUserById(idBidder) == null) {
            return ResponseEntity.badRequest().body("Bidder not found with id: " + idBidder);
        }

        if (auctionService.findAuctionById(idAuction) == null) {
            return ResponseEntity.badRequest().body("Auction not found with id: " + idAuction);
        }

        try {

            Auction updatedAuction =auctionService.placeBid(idAuction, idBidder, bidAmount);

            Notification notif=notificationService.save(updatedAuction.getSellerId(),updatedAuction.getId());

            return ResponseEntity.ok(Map.of("auction",updatedAuction,"notification",notif));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
        
        
    }
