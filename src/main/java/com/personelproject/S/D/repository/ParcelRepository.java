package com.personelproject.S.D.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.personelproject.S.D.model.Parcel;

@Repository
public interface ParcelRepository extends MongoRepository<Parcel, String> {

    Optional<Parcel> findById(String id);

    List<Parcel> findAll();

    List<Parcel> findByAdminId(String adminId);

    List<Parcel> findByTransporterId(String transporterId);

    List<Parcel> findByBuyerId(String buyerId);
}