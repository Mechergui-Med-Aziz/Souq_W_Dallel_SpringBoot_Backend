package com.personelproject.S.D.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.personelproject.S.D.model.Auction;
import com.personelproject.S.D.service.AuctionService;
import com.personelproject.S.D.service.PhotoService;

import tools.jackson.databind.ObjectMapper;



@RestController
@RequestMapping("api/auctions")
public class AuctionController {
    @Autowired
    private AuctionService auctionService;
    @Autowired
    private PhotoService photoService;


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
     
}
