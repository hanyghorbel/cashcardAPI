package com.cashcard;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

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
        // for post endpoint we need to return a location uri and optionally the dto as well.
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cashCardDto.id())
                .toUri();
        return ResponseEntity.created(location).body(cashCardDto);
    }
    @GetMapping()
    ResponseEntity<List<CashCardDto>> getAllCashCards(Pageable pageable){
        List<CashCardDto> cashCards = this.cashCardService.getAllCashCards(pageable);
        return ResponseEntity.ok(cashCards);
    }
}
