package com.example.campusmaster.service;

import com.example.campusmaster.entity.Review;
import java.util.List;

public interface ReviewService {

    Review createReview(Long taskId, Long reviewerId, Long revieweeId, Integer rating, String content);

    Review getReviewByTaskId(Long taskId);

    Review[] getTaskReviews(Long taskId);

    Review[] getUserReviews(Long userId);

    List<Review> getReviewsByRevieweeId(Long revieweeId);

    List<Review> getReviewsByReviewerId(Long reviewerId);

    Integer calculateCreditChange(Integer rating);
}
