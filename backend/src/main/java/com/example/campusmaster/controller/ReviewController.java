package com.example.campusmaster.controller;

import com.example.campusmaster.annotation.RequiresRoles;
import com.example.campusmaster.common.JwtUtil;
import com.example.campusmaster.common.Result;
import com.example.campusmaster.entity.Review;
import com.example.campusmaster.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    @RequiresRoles({"requester", "helper", "admin"})
    public Result<Void> createReview(@Valid @RequestBody CreateReviewRequest request, HttpServletRequest httpRequest) {
        Long reviewerId = (Long) httpRequest.getAttribute("userId");
        reviewService.createReview(request.getTaskId(), reviewerId, request.getRevieweeId(), request.getRating(), request.getContent());
        return Result.success("评价成功", null);
    }

    @GetMapping("/task/{taskId}")
    @RequiresRoles({"requester", "helper", "admin"})
    public Result<Review[]> getTaskReviews(@PathVariable Long taskId) {
        Review[] reviews = reviewService.getTaskReviews(taskId);
        return Result.success(reviews);
    }

    @GetMapping("/user/{userId}")
    @RequiresRoles({"requester", "helper", "admin"})
    public Result<Review[]> getUserReviews(@PathVariable Long userId) {
        Review[] reviews = reviewService.getUserReviews(userId);
        return Result.success(reviews);
    }

    public static class CreateReviewRequest {
        @NotNull(message = "任务ID不能为空")
        private Long taskId;

        @NotNull(message = "被评价人ID不能为空")
        private Long revieweeId;

        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分最低为1分")
        @Max(value = 5, message = "评分最高为5分")
        private Integer rating;

        private String content;

        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public Long getRevieweeId() { return revieweeId; }
        public void setRevieweeId(Long revieweeId) { this.revieweeId = revieweeId; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
