package com.cashcard;

import exceptions.CashCardNotFoundExcpetion;
import org.springframework.stereotype.Service;

@Service
class CashCardService {
    private final CashCardRepository cashCardRepository;

    CashCardService(CashCardRepository cash){
        this.cashCardRepository=cash;
    }

    public CashCardDto getCashCard(Long id) {
        CashCardEntity cashCardEntity= this.cashCardRepository.findById(id).orElseThrow(()-> new CashCardNotFoundExcpetion(id));
        return new CashCardDto(cashCardEntity.getId(), cashCardEntity.getBalance());
    }

    public CashCardDto createCashCard(CashCardDto cashCardDto) {
        CashCardEntity cashCard = new CashCardEntity();
        cashCard.setBalance(cashCardDto.balance());
        CashCardEntity savedCard =this.cashCardRepository.save(cashCard);
        return new CashCardDto(savedCard.getId(),savedCard.getBalance());
    }
}
