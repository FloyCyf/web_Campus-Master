package com.example.campusmaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.campusmaster.common.BusinessException;
import com.example.campusmaster.entity.Review;
import com.example.campusmaster.mapper.ReviewMapper;
import com.example.campusmaster.service.NotificationService;
import com.example.campusmaster.service.ReviewService;
import com.example.campusmaster.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public Review createReview(Long taskId, Long reviewerId, Long revieweeId, Integer rating, String content) {
        Review existingReview = reviewMapper.selectByTaskId(taskId);
        if (existingReview != null) {
            throw BusinessException.conflict("该任务已评价");
        }

        if (rating < 1 || rating > 5) {
            throw BusinessException.badRequest("评分必须在1-5之间");
        }

        Review review = new Review();
        review.setTaskId(taskId);
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setRating(rating);
        review.setContent(content);

        reviewMapper.insert(review);

        int creditChange = calculateCreditChange(rating);
        userService.updateCreditScore(revieweeId, creditChange);

        notificationService.sendNotification(
                revieweeId, taskId, "system", "收到评价",
                "您收到了一条评价，评分为" + rating + "星"
        );

        log.info("创建评价成功: taskId={}, reviewerId={}, rating={}", taskId, reviewerId, rating);
        return review;
    }

    @Override
    public Review getReviewByTaskId(Long taskId) {
        return reviewMapper.selectByTaskId(taskId);
    }

    @Override
    public Review[] getTaskReviews(Long taskId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getTaskId, taskId);
        List<Review> list = reviewMapper.selectList(wrapper);
        return list.toArray(new Review[0]);
    }

    @Override
    public Review[] getUserReviews(Long userId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRevieweeId, userId);
        List<Review> list = reviewMapper.selectList(wrapper);
        return list.toArray(new Review[0]);
    }

    @Override
    public List<Review> getReviewsByRevieweeId(Long revieweeId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRevieweeId, revieweeId);
        return reviewMapper.selectList(wrapper);
    }

    @Override
    public Integer calculateCreditChange(Integer rating) {
        return switch (rating) {
            case 5 -> 2;
            case 4 -> 1;
            case 3 -> 0;
            case 2 -> -2;
            case 1 -> -5;
            default -> 0;
        };
    }
}
