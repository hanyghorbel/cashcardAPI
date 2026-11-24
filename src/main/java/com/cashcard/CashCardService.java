package com.cashcard;

import exceptions.CashCardNotFoundExcpetion;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
class CashCardService {
    private final CashCardRepository cashCardRepository;

    CashCardService(CashCardRepository cash) {
        this.cashCardRepository = cash;
    }

    public CashCardDto getCashCard(Long id, Principal principal) {
        CashCardEntity cashCardEntity = this.cashCardRepository.findByIdAndOwner(id, principal.getName()).orElseThrow(() -> new CashCardNotFoundExcpetion(id));
        return this.toDto(cashCardEntity);



    }

    public CashCardDto createCashCard(CashCardDto cashCardDto, Principal principal) {
        CashCardEntity cashCard = new CashCardEntity();
        cashCard.setBalance(cashCardDto.balance());
        cashCard.setOwner(principal.getName());
        CashCardEntity savedCard = this.cashCardRepository.save(cashCard);
        return this.toDto(savedCard);
    }

    public List<CashCardDto> getAllCashCards(Pageable pageable, Principal principal) {
        Page<CashCardEntity> page = this.cashCardRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "balance"
                                )
                        )
                )
        );
        return page.get()
                .map(this::toDto)
                .toList();
    }

    // helper method
    private CashCardDto toDto(CashCardEntity entity) {
        return new CashCardDto(
                entity.getId(),
                entity.getBalance(),
                entity.getOwner()
        );
    }
    @Transactional // ensures the read, modfiy, update happens in one transaction
    public CashCardDto replaceCashCard(Long id, CashCardDto cashCardDto, String username) {
        CashCardEntity cashCard = this.cashCardRepository.findByIdAndOwner(id, username).orElseThrow(() -> new CashCardNotFoundExcpetion(id));
        cashCard.setBalance(cashCardDto.balance());
        CashCardEntity saved = this.cashCardRepository.save(cashCard);
        return this.toDto(saved);
    }

    public void deleteCashCard(Long id, String name) {
        this.cashCardRepository.deleteByIdAndOwner(id, name);
    }
}
