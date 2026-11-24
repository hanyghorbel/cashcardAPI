package com.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CashcardApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldReturnACashCardWhenDataIsSaved() {
		ResponseEntity<String> response= restTemplate
				.withBasicAuth("sarah", "abc123")
				.getForEntity("/cashcard/22",String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		Number id= documentContext.read("$.id");
		assertThat(id).isNotNull();
		assertThat(id).isEqualTo(22);
		Double balance=documentContext.read("$.balance");
		assertThat(balance).isNotNull();
		assertThat(balance).isEqualTo(23.6);

	}
	@Test
	void shouldNotReturnACashCardWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.getForEntity("/cashcard/1000", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}
	@Test
	@DirtiesContext
	void shouldCreateANewCashCard(){
		CashCardEntity cashCard= new CashCardEntity();
		cashCard.setBalance(123.45);
		ResponseEntity<CashCardDto> response= restTemplate
				.withBasicAuth("sarah", "abc123")
				.postForEntity("/cashcard",cashCard,CashCardDto.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().id()).isNotNull();
		assertThat(response.getBody().balance()).isNotNull();
		assertThat(response.getBody().balance()).isEqualTo(123.45);

		URI locationOfNewCashCard = response.getHeaders().getLocation();
		assertThat(locationOfNewCashCard).isNotNull();
		// we get the entity and ensure it's defined
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah", "abc123")
				.getForEntity(locationOfNewCashCard, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void shouldReturnAllCashcards(){
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.getForEntity("/cashcard", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int cashCardCount = documentContext.read("$.length()");
		assertThat(cashCardCount).isEqualTo(3);

		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(22, 100, 101);

		JSONArray amounts = documentContext.read("$..balance");
		assertThat(amounts).containsExactlyInAnyOrder(23.6, 1.0, 150.00);
	}

	@Test
	void shouldReturnAPageOfCashCards() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.getForEntity("/cashcard?page=0&size=1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);
	}
	@Test
	void shouldReturnASortedPageOfCashCards() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.getForEntity("/cashcard?page=0&size=1&sort=balance,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray read = documentContext.read("$[*]");
		assertThat(read.size()).isEqualTo(1);

		double balance = documentContext.read("$[0].balance");
		assertThat(balance).isEqualTo(150.00);
	}

	@Test
	void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.getForEntity("/cashcard", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(3);

		JSONArray balances = documentContext.read("$..balance");
		assertThat(balances).containsExactly(150.00, 23.6, 1.00);
	}

	@Test
	void shouldNotReturnACashCardWhenUserUnknown() {
		ResponseEntity<String> response= restTemplate
				.withBasicAuth("unknown", "abc123")
				.getForEntity("/cashcard/22",String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		response = restTemplate
				.withBasicAuth("sarah", "BAD-PASSWORD")
				.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldRejectUsersWhoAreNotCardOwners() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("keith", "magtop")
				.getForEntity("/cashcard/100", String.class);
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
	@DirtiesContext
	void shouldUpdateAnExistingCashCard() {
		CashCardDto cashCardUpdate = new CashCardDto(null, 19.99, null);
		HttpEntity<CashCardDto> request = new HttpEntity<>(cashCardUpdate);
		ResponseEntity<CashCardDto> response = restTemplate
				.withBasicAuth("sarah", "abc123")
				.exchange("/cashcard/22", HttpMethod.PUT, request, CashCardDto.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().id()).isEqualTo(22);
		assertThat(response.getBody().balance()).isEqualTo(19.99);
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
}
