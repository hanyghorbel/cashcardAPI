package com.cashcard;

import exceptions.CashCardNotFoundExcpetion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class CashCardService {
    private final CashCardRepository cashCardRepository;

    CashCardService(CashCardRepository cash) {
        this.cashCardRepository = cash;
    }

    public CashCardDto getCashCard(Long id) {
        CashCardEntity cashCardEntity = this.cashCardRepository.findById(id).orElseThrow(() -> new CashCardNotFoundExcpetion(id));
        return new CashCardDto(cashCardEntity.getId(), cashCardEntity.getBalance());
    }

    public CashCardDto createCashCard(CashCardDto cashCardDto) {
        CashCardEntity cashCard = new CashCardEntity();
        cashCard.setBalance(cashCardDto.balance());
        CashCardEntity savedCard = this.cashCardRepository.save(cashCard);
        return new CashCardDto(savedCard.getId(), savedCard.getBalance());
    }

    public List<CashCardDto> getAllCashCards(Pageable pageable) {
        Page<CashCardEntity> page = this.cashCardRepository.findAll(
                PageRequest.of(pageable.getPageNumber(),
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
                entity.getBalance()
        );
    }
}
