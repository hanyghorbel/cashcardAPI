package com.cashcard;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.Valid;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/cashcard")
class CashCardController {
    private final CashCardService cashCardService;

    CashCardController(CashCardService cashCardService) {
        this.cashCardService = cashCardService;
    }

    @GetMapping("/{id}")
    ResponseEntity<CashCardDto> getCashCard(@PathVariable Long id, Principal principal) {
        CashCardDto cashCardDto = this.cashCardService.getCashCard(id, principal);
        return ResponseEntity.ok(cashCardDto);
    }

    @PostMapping()
    ResponseEntity<CashCardDto> createCashCard(@Valid @RequestBody CashCardDto cashCard, Principal principal){
        CashCardDto cashCardDto = this.cashCardService.createCashCard(cashCard, principal);
        // for post endpoint we need to return a location uri and optionally the dto as well.
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cashCardDto.id())
                .toUri();
        return ResponseEntity.created(location).body(cashCardDto);
    }
    @GetMapping()
    ResponseEntity<List<CashCardDto>> getAllCashCards(Pageable pageable, Principal principal){
        List<CashCardDto> cashCards = this.cashCardService.getAllCashCards(pageable, principal);
        return ResponseEntity.ok(cashCards);
    }

    @PutMapping("/{id}")
    ResponseEntity<CashCardDto> replaceCashCard(@PathVariable Long id,@Valid @RequestBody CashCardDto cashCardDto, Principal principal){
        return ResponseEntity.status(HttpStatus.OK).body(this.cashCardService.replaceCashCard(id, cashCardDto, principal.getName()));
    }
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal){
        this.cashCardService.deleteCashCard(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
