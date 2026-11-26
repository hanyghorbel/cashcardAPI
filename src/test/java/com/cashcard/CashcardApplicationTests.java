package com.cashcard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CashcardApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	@Autowired
	CashCardRepository cashCardRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldReturnACashCardWhenDataIsSaved() {
		ResponseEntity<CashCardDto> response= restTemplate
				.withBasicAuth("sarah", "abc123")
				.getForEntity("/cashcard/22",CashCardDto.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		CashCardDto cashCardDto = response.getBody();
		assertThat(cashCardDto).isNotNull();
		assertThat(cashCardDto.id()).isNotNull();
		assertThat(cashCardDto.id()).isEqualTo(22);
		assertThat(cashCardDto.balance()).isNotNull();
		assertThat(cashCardDto.balance()).isEqualTo(23.6);

	}
	@Test
	void shouldNotReturnACashCardWithAnUnknownId() {
		ResponseEntity<CashCardDto> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.getForEntity("/cashcard/1000", CashCardDto.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	@Test
	void shouldCreateANewCashCard(){
		CashCardEntity cashCard= new CashCardEntity();
		cashCard.setBalance(123.45);

		// creating a cashCard
		ResponseEntity<CashCardDto> response= restTemplate
				.withBasicAuth("sarah", "abc123")
				.postForEntity("/cashcard",cashCard,CashCardDto.class);
		CashCardDto cashCardDto = response.getBody();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(cashCardDto).isNotNull();
		assertThat(cashCardDto.id()).isNotNull();
		assertThat(cashCardDto.balance()).isNotNull();
		assertThat(cashCardDto.balance()).isEqualTo(123.45);

		URI locationOfNewCashCard = response.getHeaders().getLocation();
		assertThat(locationOfNewCashCard).isNotNull();

		// retrieve the created entity and check it's defined
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah", "abc123")
				.getForEntity(locationOfNewCashCard, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		// Delete the created cashcard to keep the db in a stable state
		this.cashCardRepository.deleteById(cashCardDto.id());
	}

	@Test
	void shouldReturnAllCashcards(){
		ResponseEntity<CashCardDto[]> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.getForEntity("/cashcard", CashCardDto[].class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		CashCardDto[] cashCardDtos= response.getBody();
		assertThat(cashCardDtos).isNotEmpty();
		assertThat(cashCardDtos.length).isEqualTo(3);

		assertThat(cashCardDtos)
				.extracting(CashCardDto::id)
				.containsExactlyInAnyOrder(22L, 100L, 101L);

		assertThat(cashCardDtos)
				.extracting(CashCardDto::balance)
				.containsExactlyInAnyOrder(23.6, 1.0, 150.00);
	}

	@Test
	void shouldReturnAPageOfCashCards() {
		ResponseEntity<CashCardDto[]> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.getForEntity("/cashcard?page=0&size=1", CashCardDto[].class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		CashCardDto[] cashCardDtos= response.getBody();
		assertThat(cashCardDtos).isNotEmpty();
		assertThat(cashCardDtos.length).isEqualTo(1);
	}
	@Test
	void shouldReturnASortedPageOfCashCards() {
		ResponseEntity<CashCardDto[]> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.getForEntity("/cashcard?page=0&size=1&sort=balance,desc", CashCardDto[].class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		CashCardDto[] cashCardDtos= response.getBody();
		assertThat(cashCardDtos).isNotEmpty();
		assertThat(cashCardDtos.length).isEqualTo(1);


		assertThat(cashCardDtos)
				.extracting(CashCardDto::balance)
				.isEqualTo(List.of(150.00));
	}

	@Test
	void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
		ResponseEntity<CashCardDto[]> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.getForEntity("/cashcard", CashCardDto[].class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		CashCardDto[] cashCardDtos = response.getBody();
		assertThat(cashCardDtos).isNotEmpty();
		assertThat(cashCardDtos.length).isEqualTo(3);

		assertThat(cashCardDtos)
				.extracting(CashCardDto::balance)
				.containsExactly(150.00, 23.6, 1.00);
	}

	@Test
	void shouldNotReturnACashCardWhenUserUnknown() {
		ResponseEntity<CashCardDto> response = restTemplate
				.withBasicAuth("unknown", "abc123")
				.getForEntity("/cashcard/22", CashCardDto.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldNotReturnACashCardWhenPasswordWrong() {
		ResponseEntity<CashCardDto> response = restTemplate
				.withBasicAuth("sarah", "BAD-PASSWORD")
				.getForEntity("/cashcards/99", CashCardDto.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldRejectUsersWhoAreNotCardOwners() {
		ResponseEntity<CashCardDto> response = restTemplate
				.withBasicAuth("keith", "magtop")
				.getForEntity("/cashcard/100", CashCardDto.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldRejectUserWhoAccessesACardNotHis(){
		ResponseEntity<CashCardDto> response = restTemplate
				.withBasicAuth("vishal", "123")
				.getForEntity("/cashcard/101", CashCardDto.class);
		// vishal does not own card 101. should fail
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldUpdateAnExistingCashCard() {
		CashCardDto cashCardUpdate = new CashCardDto(null, 19.99, null);
		HttpEntity<CashCardDto> request = new HttpEntity<>(cashCardUpdate);
		ResponseEntity<CashCardDto> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.exchange("/cashcard/22", HttpMethod.PUT, request, CashCardDto.class);
		CashCardDto updated = response.getBody();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(updated).isNotNull();
		assertThat(updated.id()).isEqualTo(22);
		assertThat(updated.balance()).isEqualTo(19.99);

		// setting back the db to a stable state
		CashCardEntity cashCard= CashCardEntity.builder().id(22L).balance(23.6).owner("sarah").build();
		this.cashCardRepository.save(cashCard);
	}

	@Test
	void shouldNotUpdateACashCardThatDoesNotExist() {
		CashCardDto unknownCard = new CashCardDto(null, 19.99, null);
		HttpEntity<CashCardDto> request = new HttpEntity<>(unknownCard);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.exchange("/cashcard/99999", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotUpdateACashCardThatIsOwnedBySomeoneElse() {
		CashCardDto kumarsCard = new CashCardDto(null, 333.33, null);
		HttpEntity<CashCardDto> request = new HttpEntity<>(kumarsCard);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.exchange("/cashcard/102", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldDeleteACashCard(){
		// cashcard with balance 23.6 and owner sarah
		CashCardEntity cashCard = this.cashCardRepository.save(CashCardEntity.builder().balance(23.6).owner("sarah").build());
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.exchange("cashcard/"+cashCard.getId(),HttpMethod.DELETE,null,Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	void shouldNotDeleteACashCardThatIsOwnedBySomeoneElse(){
		// cashcard with id 102 has balance 150.00 and owner vishal
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.exchange("cashcard/102",HttpMethod.DELETE,null,Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotDeleteACashCardThatDoesNotExist(){
		// cashcard with id 102 has balance 150.00 and owner vishal
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.exchange("cashcard/10200",HttpMethod.DELETE,null,Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}
