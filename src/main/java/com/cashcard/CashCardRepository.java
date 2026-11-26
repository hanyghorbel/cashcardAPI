package com.cashcard;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CashCardRepository extends JpaRepository<CashCardEntity,Long> {
    Optional<CashCardEntity> findByIdAndOwner(Long id, String owner);
    @Transactional
    Long deleteByIdAndOwner(Long id, String owner);

    Page<CashCardEntity> findByOwner(String owner, Pageable pageable);
}
