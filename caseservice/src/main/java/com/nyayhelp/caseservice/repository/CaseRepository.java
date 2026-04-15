package com.nyayhelp.caseservice.repository;

import com.nyayhelp.caseservice.model.Case;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseRepository extends JpaRepository<Case, Long> {

    List<Case> findByStatus(String status);

    List<Case> findByStatusAndCategoryAndLocation(String status, String category, String location);
}