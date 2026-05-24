package com.nyayhelp.reviewservice.dto;

public class RatingSummary {
    public Long lawyerId;
    public Double average;
    public Long count;

    public RatingSummary() {}

    public RatingSummary(Long lawyerId, Double average, Long count) {
        this.lawyerId = lawyerId;
        this.average = average;
        this.count = count;
    }
}
