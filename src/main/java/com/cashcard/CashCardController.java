package com.cashcard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cashcard")
class CashCardController {
    private final CashCardService cashCardService;

    CashCardController(CashCardService cashCardService) {
        this.cashCardService = cashCardService;
    }

    @GetMapping("/{id}")
    ResponseEntity<CashCardDto> getCashCard(@PathVariable Long id) {
        CashCardDto cashCardDto = this.cashCardService.getCashCard(id);
        return ResponseEntity.ok(cashCardDto);
    }

    @PostMapping()
    ResponseEntity<CashCardDto> createCahsCard(@RequestBody CashCardDto cashCard){
        CashCardDto cashCardDto = this.cashCardService.createCashCard(cashCard);
        return ResponseEntity.ok(cashCardDto);
    }
}
