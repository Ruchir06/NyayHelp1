package com.nyayhelp.chatservice.repository;

import com.nyayhelp.chatservice.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByCaseIdOrderByTimestampAsc(Long caseId);
}