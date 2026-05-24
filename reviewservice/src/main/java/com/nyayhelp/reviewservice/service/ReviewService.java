package com.nyayhelp.reviewservice.service;

import com.nyayhelp.reviewservice.dto.CaseResponse;
import com.nyayhelp.reviewservice.dto.RatingSummary;
import com.nyayhelp.reviewservice.dto.ReviewRequest;
import com.nyayhelp.reviewservice.dto.ReviewResponse;
import com.nyayhelp.reviewservice.dto.UserProfileResponse;
import com.nyayhelp.reviewservice.model.Review;
import com.nyayhelp.reviewservice.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository repository;

    @Autowired
    private RestClient restClient;

    public ReviewResponse createReview(ReviewRequest request, Authentication authentication) {

        if (request == null || request.caseId == null || request.rating == null) {
            throw new RuntimeException("caseId and rating are required");
        }
        if (request.rating < 1 || request.rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        Long reviewerId = (Long) authentication.getDetails();
        String reviewerRole = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "");

        if (!"CLIENT".equals(reviewerRole)) {
            throw new RuntimeException("Only clients can submit reviews.");
        }

        CaseResponse caseData = restClient.get()
                .uri("http://localhost:8083/api/cases/" + request.caseId)
                .retrieve()
                .body(CaseResponse.class);

        if (caseData == null) {
            throw new RuntimeException("Case not found.");
        }
        if (!"ASSIGNED".equals(caseData.status) || caseData.lawyerId == null) {
            throw new RuntimeException("Review allowed only after a lawyer is assigned to the case.");
        }
        if (!reviewerId.equals(caseData.clientId)) {
            throw new RuntimeException("Only the case owner can review the assigned lawyer.");
        }
        if (repository.existsByCaseIdAndReviewerId(request.caseId, reviewerId)) {
            throw new RuntimeException("You have already submitted a review for this case.");
        }

        Review review = new Review();
        review.setCaseId(request.caseId);
        review.setLawyerId(caseData.lawyerId);
        review.setReviewerId(reviewerId);
        review.setRating(request.rating);
        review.setComment(request.comment);
        review.setCreatedAt(LocalDateTime.now());

        Review saved = repository.save(review);
        return toResponse(saved);
    }

    public List<ReviewResponse> getReviewsForLawyer(Long lawyerId) {
        return repository.findByLawyerIdOrderByCreatedAtDesc(lawyerId).stream()
                .map(this::toResponse)
                .toList();
    }

    public RatingSummary getSummaryForLawyer(Long lawyerId) {
        Double avg = repository.averageRatingForLawyer(lawyerId);
        long count = repository.countByLawyerId(lawyerId);
        return new RatingSummary(lawyerId, avg == null ? 0.0 : avg, count);
    }

    public ReviewResponse getMyReviewForCase(Long caseId, Authentication authentication) {
        Long reviewerId = (Long) authentication.getDetails();
        Optional<Review> existing = repository.findByCaseIdAndReviewerId(caseId, reviewerId);
        return existing.map(this::toResponse).orElse(null);
    }

    private ReviewResponse toResponse(Review review) {
        ReviewResponse res = new ReviewResponse();
        res.id = review.getId();
        res.caseId = review.getCaseId();
        res.lawyerId = review.getLawyerId();
        res.reviewerId = review.getReviewerId();
        res.rating = review.getRating();
        res.comment = review.getComment();
        res.createdAt = review.getCreatedAt() == null ? null : review.getCreatedAt().toString();

        try {
            UserProfileResponse user = restClient.get()
                    .uri("http://localhost:8082/api/users/by-auth/" + review.getReviewerId())
                    .retrieve()
                    .body(UserProfileResponse.class);
            res.reviewerName = user == null ? "Anonymous" : user.name;
        } catch (Exception e) {
            res.reviewerName = "Anonymous";
        }

        return res;
    }
}
