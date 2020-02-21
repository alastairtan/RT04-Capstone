package capstone.rt04.retailbackend.services;

import capstone.rt04.retailbackend.entities.*;
import capstone.rt04.retailbackend.repositories.ReviewRepository;
import capstone.rt04.retailbackend.util.exceptions.InputDataValidationException;
import capstone.rt04.retailbackend.util.exceptions.customer.CustomerNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.product.ProductNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.review.CreateNewReviewException;
import capstone.rt04.retailbackend.util.exceptions.review.ReviewNotDeletedException;
import capstone.rt04.retailbackend.util.exceptions.review.ReviewNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.review.ReviewNotUpdatedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ReviewService {

    private final ValidationService validationService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final StaffService staffService;

    private final ReviewRepository reviewRepository;

    public ReviewService(ValidationService validationService, ProductService productService, CustomerService customerService, StaffService staffService, ReviewRepository reviewRepository) {
        this.validationService = validationService;
        this.productService = productService;
        this.customerService = customerService;
        this.staffService = staffService;
        this.reviewRepository = reviewRepository;
    }

    public Review createNewReview(Review review, Long productId, Long customerId) throws CreateNewReviewException, InputDataValidationException {
        Map<String, String> errorMap = validationService.generateErrorMap(review);
        if (errorMap != null){
            throw new InputDataValidationException(errorMap, "Review is invalid!");
        }
        try {
            Review newReview = reviewRepository.save(review);
            Product product = productService.retrieveProductById(productId);
            product.getReviews().add(newReview);
            Customer customer = customerService.retrieveCustomerByCustomerId(customerId);
            customer.getReviews().add(newReview);
        } catch (PersistenceException | ProductNotFoundException | CustomerNotFoundException ex){
            throw new CreateNewReviewException("Error creating new review");
        }
        return review;
    }

    public List<Review> retrieveAllReviewsByProductId(Long productId) throws ProductNotFoundException, ReviewNotFoundException {
        if (productId == null) {
            throw new ProductNotFoundException("Product ID not provided");
        }
        List<Review> reviews = reviewRepository.findAllByProduct_ProductId(productId);
        lazilyLoadReview(reviews);
        return reviews;
    }

    public Review retrieveReviewById(Long reviewId) throws ReviewNotFoundException {
        if (reviewId == null) {
            throw new ReviewNotFoundException("Review ID not provided");
        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review ID " + reviewId + " does not exist!"));

        List<Review> reviews = new ArrayList<>();
        reviews.add(review);
        lazilyLoadReview(reviews);
        return review;
    }

    public Review updateReview(Review review) throws InputDataValidationException, ReviewNotUpdatedException, ReviewNotFoundException {
        Map<String, String> errorMap = validationService.generateErrorMap(review);
        if (errorMap == null) {
            try {
                Review reviewToUpdate = retrieveReviewById(review.getReviewId());
                reviewToUpdate.setRating(review.getRating());
                reviewToUpdate.setResponse(review.getResponse());
                reviewToUpdate.setContent(review.getContent());
                reviewRepository.save(reviewToUpdate);
                return reviewToUpdate;
            } catch (ReviewNotFoundException ex) {
                throw new ReviewNotUpdatedException("Error updating review.");
            }
        }
        return review;
    }

    public Review deleteReview(Long reviewId) throws ReviewNotFoundException, ReviewNotDeletedException, ProductNotFoundException, CustomerNotFoundException {
        Review reviewToDelete = retrieveReviewById(reviewId);
        if (reviewId == null) {
            throw new ReviewNotFoundException("Review ID not provided");
        }
        Product product = productService.retrieveProductById(reviewToDelete.getProduct().getProductId());
        product.getReviews().remove(reviewToDelete);
        Customer customer = customerService.retrieveCustomerByCustomerId(reviewToDelete.getCustomer().getCustomerId());
        customer.getReviews().remove(reviewToDelete);

        reviewRepository.delete(reviewToDelete);
        return reviewToDelete;
    }

    public void lazilyLoadReview(List<Review> reviews) {
        for (Review review : reviews) {
            review.getResponse();
            review.getRating();
            review.getContent();
        }
    }
}
