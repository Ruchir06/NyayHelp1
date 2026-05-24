package com.nyayhelp.reviewservice.controller;

import com.nyayhelp.reviewservice.dto.RatingSummary;
import com.nyayhelp.reviewservice.dto.ReviewRequest;
import com.nyayhelp.reviewservice.dto.ReviewResponse;
import com.nyayhelp.reviewservice.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService service;

    @PostMapping
    public ReviewResponse create(@RequestBody ReviewRequest request, Authentication authentication) {
        return service.createReview(request, authentication);
    }

    @GetMapping("/lawyer/{lawyerId}")
    public List<ReviewResponse> getForLawyer(@PathVariable Long lawyerId) {
        return service.getReviewsForLawyer(lawyerId);
    }

    @GetMapping("/lawyer/{lawyerId}/summary")
    public RatingSummary getSummary(@PathVariable Long lawyerId) {
        return service.getSummaryForLawyer(lawyerId);
    }

    @GetMapping("/case/{caseId}/mine")
    public ReviewResponse getMine(@PathVariable Long caseId, Authentication authentication) {
        return service.getMyReviewForCase(caseId, authentication);
    }
}
