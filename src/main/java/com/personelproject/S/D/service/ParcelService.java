package com.personelproject.S.D.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.personelproject.S.D.model.Parcel;
import com.personelproject.S.D.repository.ParcelRepository;

@Service
public class ParcelService {
    @Autowired
    private ParcelRepository parcelRepository;

    public Parcel saveParcel(Parcel parcel) {
        return parcelRepository.save(parcel);
    }

    public Parcel findParcelById(String id) {
        return parcelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Parcel not found with id: " + id));
    }

    public List<Parcel> findAllParcels() {
        return parcelRepository.findAll();
    }

    public void deleteParcel(String id) {
        if (!parcelRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Parcel not found with id: " + id);
        }
        parcelRepository.deleteById(id);
    }

    public Parcel updateParcel(Parcel parcel) {
        if (!parcelRepository.existsById(parcel.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Parcel not found with id: " + parcel.getId());
        }
        return parcelRepository.save(parcel);
    }

    public Parcel validateUnvalidateParcel(String id, Boolean isValid, String description) {
        Parcel parcel = findParcelById(id);
        parcel.setIsValid(isValid);
        parcel.setUnvalidDescription(description);
        return saveParcel(parcel);
    }

    public Parcel delivredParcel(String id) {
        Parcel parcel = findParcelById(id);
        parcel.setDelivred(true);
        return saveParcel(parcel);
    }

    public List<Parcel> findByAdminId(String id) {
        return parcelRepository.findByAdminId(id);
    }

    public List<Parcel> findByTransporterId(String transporterId) {
        return parcelRepository.findByTransporterId(transporterId);
    }

    public List<Parcel> findByBuyerId(String buyerId) {
        return parcelRepository.findByBuyerId(buyerId);
    }
}