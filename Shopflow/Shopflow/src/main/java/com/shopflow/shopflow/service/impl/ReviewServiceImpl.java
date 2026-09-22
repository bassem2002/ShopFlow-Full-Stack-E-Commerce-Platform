package com.shopflow.shopflow.service.impl;

import com.shopflow.shopflow.dto.request.ReviewRequest;
import com.shopflow.shopflow.dto.response.ReviewResponse;
import com.shopflow.shopflow.entity.Product;
import com.shopflow.shopflow.entity.Review;
import com.shopflow.shopflow.entity.User;
import com.shopflow.shopflow.exception.BusinessException;
import com.shopflow.shopflow.repository.OrderRepository;
import com.shopflow.shopflow.repository.ProductRepository;
import com.shopflow.shopflow.repository.ReviewRepository;
import com.shopflow.shopflow.repository.UserRepository;
import com.shopflow.shopflow.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public ReviewResponse addReview(Long userId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("Product not found"));

        if (!orderRepository.hasBeenDeliveredToUser(userId, product.getId())) {
            throw new BusinessException("Vous ne pouvez laisser un avis que pour les produits qui vous ont été livrés.");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        return mapToResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long userId, Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException("You can only update your own reviews");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return mapToResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException("You can only delete your own reviews");
        }

        reviewRepository.deleteById(reviewId);
    }

    @Override
    public ReviewResponse getReviewById(Long id) {
        return reviewRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new BusinessException("Review not found"));
    }

    @Override
    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
