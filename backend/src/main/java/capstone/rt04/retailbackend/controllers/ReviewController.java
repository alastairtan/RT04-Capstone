package capstone.rt04.retailbackend.controllers;

import capstone.rt04.retailbackend.entities.Customer;
import capstone.rt04.retailbackend.entities.Review;
import capstone.rt04.retailbackend.request.review.ReviewCreateRequest;
import capstone.rt04.retailbackend.response.GenericErrorResponse;
import capstone.rt04.retailbackend.services.RelationshipService;
import capstone.rt04.retailbackend.services.ReviewService;
import capstone.rt04.retailbackend.services.ValidationService;
import capstone.rt04.retailbackend.util.exceptions.InputDataValidationException;
import capstone.rt04.retailbackend.util.exceptions.customer.CustomerNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.product.ProductNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.review.CreateNewReviewException;
import capstone.rt04.retailbackend.util.exceptions.review.ReviewNotDeletedException;
import capstone.rt04.retailbackend.util.exceptions.review.ReviewNotFoundException;
import capstone.rt04.retailbackend.util.routeconstants.ReviewControllerRoutes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ReviewControllerRoutes.REVIEW_BASE_ROUTE)
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ReviewController {

    private final ReviewService reviewService;
    private final ValidationService validationService;
    private final RelationshipService relationshipService;

    public ReviewController(ReviewService reviewService, ValidationService validationService, RelationshipService relationshipService) {
        this.reviewService = reviewService;
        this.validationService = validationService;
        this.relationshipService = relationshipService;
    }

    @PostMapping(ReviewControllerRoutes.CREATE_NEW_REVIEW)
    public ResponseEntity<?> createNewReview(@RequestBody ReviewCreateRequest reviewCreateRequest) throws InputDataValidationException, CreateNewReviewException, CreateNewReviewException {

        Review newReview = reviewService.createNewReview(reviewCreateRequest.getReview(), reviewCreateRequest.getProductId(),
                reviewCreateRequest.getCustomerId());
        return new ResponseEntity<>(newReview, HttpStatus.CREATED);
    }

    @GetMapping(ReviewControllerRoutes.RETRIEVE_REVIEW_BY_ID)
    public ResponseEntity<?> retrieveReviewById(@PathVariable Long reviewId) {
        try {
            Review review = reviewService.retrieveReviewById(reviewId);
            return new ResponseEntity<>(review, HttpStatus.OK);
        } catch (ReviewNotFoundException ex) {
            return new ResponseEntity<>(new GenericErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(new GenericErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(ReviewControllerRoutes.RETRIEVE_REVIEWS_BY_CUSTOMER_ID)
    public ResponseEntity<?> retrieveReviewByCustomerId(@PathVariable Long customerId) {
        List<Review> reviews = reviewService.retrieveReviewsByCustomerId(customerId);

        for(Review r : reviews) {
            Customer c = r.getCustomer();
            relationshipService.clearCustomerRelationships(c);
//            r.getCustomer().setReviews(null);
            r.getProduct().setReviews(null);
            r.setStaff(null);
        }
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @GetMapping(ReviewControllerRoutes.RETRIEVE_ALL_REVIEW_BY_PRODUCT_ID)
    public ResponseEntity<?> retrieveAllReviewByProductId(@PathVariable Long productId) {
        try {
            List<Review> reviews = reviewService.retrieveAllReviewsByProductId(productId);
            clearReviewRelationships(reviews);
            return new ResponseEntity<>(reviews, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new GenericErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(ReviewControllerRoutes.RETRIEVE_ALL_REVIEWS)
    public ResponseEntity<?> retrieveAllReviews() {
        return new ResponseEntity<>(reviewService.retrieveAllReviews(), HttpStatus.OK);
    }

    @PostMapping(ReviewControllerRoutes.UPDATE_REVIEW)
    public ResponseEntity<?> updateReview(@RequestBody ReviewCreateRequest reviewCreateRequest) {
        try {
            Review review = reviewService.updateReview(reviewCreateRequest.getReview(), reviewCreateRequest.getCustomerId(), reviewCreateRequest.getProductId());
            return new ResponseEntity<>(review, HttpStatus.OK);
        } catch (ReviewNotFoundException ex) {
            return new ResponseEntity<>(new GenericErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(new GenericErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(ReviewControllerRoutes.DELETE_REVIEW)
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) throws ReviewNotDeletedException, ReviewNotFoundException, CustomerNotFoundException, ProductNotFoundException {
        Review deletedReview = reviewService.deleteReview(reviewId);
        return new ResponseEntity<>(deletedReview, HttpStatus.OK);
    }

    @GetMapping(ReviewControllerRoutes.CHECK_IF_CAN_WRITE_REVIEW)
    public ResponseEntity<?> checkIfCanWriteReview(@PathVariable Long productId, @PathVariable  Long customerId) throws CustomerNotFoundException {
        Boolean canWrite = reviewService.checkIfAllowedToWriteReview(productId, customerId);
        return new ResponseEntity<>(canWrite, HttpStatus.OK);
    }

    private void clearReviewRelationships(List<Review> reviews) {
        for(Review review : reviews) {
            if(review.getStaff() != null) {
                review.getStaff().setRepliedReviews(null);
            }
            if(review.getProduct() != null) {
                review.getProduct().setReviews(null);
            }
            if(review.getCustomer() != null) {
                review.getCustomer().setReviews(null);
            }
        }
    }
}
