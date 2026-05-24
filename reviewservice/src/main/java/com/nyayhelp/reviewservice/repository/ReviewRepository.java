package com.nyayhelp.reviewservice.repository;

import com.nyayhelp.reviewservice.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByLawyerIdOrderByCreatedAtDesc(Long lawyerId);

    Optional<Review> findByCaseIdAndReviewerId(Long caseId, Long reviewerId);

    boolean existsByCaseIdAndReviewerId(Long caseId, Long reviewerId);

    long countByLawyerId(Long lawyerId);

    @Query("select avg(r.rating) from Review r where r.lawyerId = :lawyerId")
    Double averageRatingForLawyer(@Param("lawyerId") Long lawyerId);
}
