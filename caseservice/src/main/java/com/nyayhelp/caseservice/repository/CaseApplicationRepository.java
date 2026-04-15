package com.nyayhelp.caseservice.repository;

import com.nyayhelp.caseservice.model.CaseApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseApplicationRepository extends JpaRepository<CaseApplication, Long> {

    List<CaseApplication> findByCaseId(Long caseId);

    List<CaseApplication> findByLawyerId(Long lawyerId);
}