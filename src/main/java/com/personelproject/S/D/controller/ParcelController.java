package com.personelproject.S.D.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.personelproject.S.D.model.Parcel;
import com.personelproject.S.D.service.ParcelService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/parcels")
public class ParcelController {

    @Autowired
    private ParcelService parcelService;


    
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllParcels() {
        return ResponseEntity.ok(parcelService.findAllParcels());
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> createParcel(@PathVariable String id) { 
        return ResponseEntity.ok(parcelService.findParcelById(id));
     }

     @DeleteMapping("/delete/{id}")
     public ResponseEntity<?> deleteParcel(@PathVariable String id) {
        parcelService.deleteParcel(id);
        return ResponseEntity.ok().build();
     }
     

     @PutMapping("/update/{id}")
     public ResponseEntity<?> updateParcel(@PathVariable String id, @RequestBody Parcel parcel) {
        Parcel existingParcel=parcelService.findParcelById(id);
        if(existingParcel==null){
            return ResponseEntity.notFound().build();
        }
        existingParcel.setIsValid(parcel.getIsValid());
        existingParcel.setDestinationAdress(parcel.getDestinationAdress());
        existingParcel.setPickUpAdress(parcel.getPickUpAdress());
        existingParcel.setUnvalidDescription(parcel.getUnvalidDescription());
        existingParcel.setTransporterId(parcel.getTransporterId());
        return ResponseEntity.ok(parcelService.updateParcel(existingParcel));
         
         
     }

     @PutMapping("/validate/{id}/{isValid}/{description}")
     public ResponseEntity<?> ValidateUnvalidateParcel(@PathVariable String id, @PathVariable boolean isValid,@PathVariable String description) {
        Parcel parcel=parcelService.findParcelById(id);
        if(parcel!=null){
            return ResponseEntity.ok(parcelService);
        }
        return ResponseEntity.notFound().build();
     }

     @PutMapping("delivred/{id}")
     public ResponseEntity<?> deliveredParcel(@PathVariable String id) {
        Parcel parcel=parcelService.findParcelById(id);
        if(parcel!=null){
            return ResponseEntity.ok(parcelService.delivredParcel(id));
        }
        return ResponseEntity.notFound().build();
     }

     @GetMapping("admin/{adminId}")
     public ResponseEntity<?> getParcelsByAdmin(@PathVariable String adminId) {
         List<Parcel> parcels=parcelService.findByAdminId(adminId);
            return ResponseEntity.ok(parcels);
     }
     
    
     
}
