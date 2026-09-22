package com.shopflow.shopflow.service;

import com.shopflow.shopflow.dto.request.ReviewRequest;
import com.shopflow.shopflow.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse addReview(Long userId, ReviewRequest request);

    ReviewResponse updateReview(Long userId, Long reviewId, ReviewRequest request);

    void deleteReview(Long userId, Long reviewId);

    ReviewResponse getReviewById(Long id);

    List<ReviewResponse> getReviewsByProduct(Long productId);

    List<ReviewResponse> getReviewsByUser(Long userId);
}
