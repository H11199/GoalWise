package com.goalwise.auth.repo;

import com.goalwise.auth.entity.OtpSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OtpSessionRepository extends JpaRepository<OtpSession, UUID> {
    Optional<OtpSession> findByVonageRequestId(String vonageRequestId);
}
