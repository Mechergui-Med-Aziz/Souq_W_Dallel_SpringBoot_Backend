package com.personelproject.S.D.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;

import com.personelproject.S.D.model.Auction;
import com.personelproject.S.D.model.User;
import com.personelproject.S.D.repository.AuctionRepository;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    public Auction saveAuction(Auction auction) {
        return auctionRepository.save(auction);
    }

    public Auction findAuctionById(String id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Auction not found with id: " + id));
    }

    public List<Auction> findAllAuctions() {
        return auctionRepository.findAll();
    }

    public void deleteAuction(String id) {
        if (!auctionRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Auction not found with id: " + id);
        }
        auctionRepository.deleteById(id);
    }

    public Auction updateAuction(Auction auction) {
        if (!auctionRepository.existsById(auction.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Auction not found with id: " + auction.getId());
        }
        return auctionRepository.save(auction);
    }

    public List<Auction> findAuctionsBySellerId(String sellerId) {
        return auctionRepository.findBySellerId(sellerId);
    }

    public Auction placeBid(String auctionId, String bidderId, Double bidAmount) {
        Auction auction = findAuctionById(auctionId);
        if (auction.getBidders() == null || !auction.getBidders().containsKey(bidderId)
                || bidAmount > auction.getBidders().get(bidderId)) {
            auction.getBidders().put(bidderId, bidAmount);
            return auctionRepository.save(auction);
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Bid must be higher than current bid for bidder: " + bidderId);
        }
    }

    public boolean addReview(String auctionId, String reviewerId, String review) {
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        if (auction == null)
            return false;

        MultiValueMap<String, String> reviews = auction.getReviews();
        if (reviews == null) {
            reviews = new LinkedMultiValueMap<>();
        }

        // Add review without timestamp
        reviews.add(reviewerId, review);
        auction.setReviews(reviews);
        auctionRepository.save(auction);
        return true;
    }

    public boolean updateReview(String auctionId, String reviewerId, String oldReview, String newReview) {
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        if (auction == null)
            return false;

        MultiValueMap<String, String> reviews = auction.getReviews();
        if (reviews == null)
            return false;

        List<String> userReviews = reviews.get(reviewerId);
        if (userReviews == null)
            return false;

        // Find and replace the old review
        int index = userReviews.indexOf(oldReview);
        if (index != -1) {
            userReviews.set(index, newReview);
            reviews.put(reviewerId, userReviews);
            auction.setReviews(reviews);
            auctionRepository.save(auction);
            return true;
        }
        return false;
    }

    public boolean deleteReview(String auctionId, String reviewerId, String review) {
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        if (auction == null)
            return false;

        MultiValueMap<String, String> reviews = auction.getReviews();
        if (reviews == null)
            return false;

        List<String> userReviews = reviews.get(reviewerId);
        if (userReviews == null)
            return false;

        // Remove the review
        boolean removed = userReviews.remove(review);
        if (removed) {
            if (userReviews.isEmpty()) {
                reviews.remove(reviewerId);
            } else {
                reviews.put(reviewerId, userReviews);
            }
            auction.setReviews(reviews);
            auctionRepository.save(auction);
            return true;
        }
        return false;
    }

    public MultiValueMap<String, String> getReviews(String auctionId) {
        Auction auction = findAuctionById(auctionId);
        return auction.getReviews();
    }

    public List<Auction> findAuctionByStatus(String status){
        return auctionRepository.findByStatus(status);
    }

    public Auction DenyApproveAuction(String auctionId,String status,String adminId){
        Auction auction = findAuctionById(auctionId);
        auction.setStatus(status);
        auction.setAdminId(adminId);
        return auctionRepository.save(auction);
    }

    public User getBuyer(String auctionId){
        Auction auction=findAuctionById(auctionId);
        if(auction.getStatus().equals("ended")){
            String buyerId=auction.getBidders().entrySet().stream()
                    .max((entry1, entry2) -> Double.compare(entry1.getValue(), entry2.getValue()))
                    .map(entry -> entry.getKey())
                    .orElse(null);
            User buyer=new User();
            buyer.setId(buyerId);
            return buyer;
        }
        return null;
    } 

}