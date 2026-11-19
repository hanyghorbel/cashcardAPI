package com.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CashcardApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldReturnACashCardWhenDataIsSaved() {
		ResponseEntity<String> response= restTemplate.getForEntity("/cashcard/99",String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		Number id= documentContext.read("$.id");
		assertThat(id).isNotNull();
		assertThat(id).isEqualTo(99);
		Double balance=documentContext.read("$.balance");
		assertThat(balance).isNotNull();
		assertThat(balance).isEqualTo(123.45);

	}
	@Test
	void shouldNotReturnACashCardWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcard/1000", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}
	@Test
	void shouldCreateANewCashCard(){
		CashCardEntity cashCard= new CashCardEntity();
		cashCard.setBalance(123.45);
		ResponseEntity<CashCardDto> response= restTemplate.postForEntity("/cashcard",cashCard,CashCardDto.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody().id()).isNotNull();
		assertThat(response.getBody().balance()).isNotNull();
		assertThat(response.getBody().balance()).isEqualTo(123.45);

		URI locationOfNewCashCard = response.getHeaders().getLocation();
		assertThat(locationOfNewCashCard).isNotNull();
		// we get the entity and ensure it's defined
		ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

}
