package com.jobstream.api.repository;

import com.jobstream.api.entity.UserCv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCvRepository extends JpaRepository<UserCv, Long> {
    Optional<UserCv> findByUserId(Long userId);
}
