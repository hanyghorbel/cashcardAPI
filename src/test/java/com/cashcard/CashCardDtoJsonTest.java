package com.cashcard;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CashCardDtoJsonTest {

    @Autowired
    private JacksonTester<CashCardDto> json;

    @Autowired
    private JacksonTester<CashCardDto[]> jsonList;

    private CashCardDto[] cashCards;

    @BeforeEach
    void setUp(){
        this.cashCards = Arrays.array(
                new CashCardDto(22L,23.6),
                new CashCardDto(100L, 1.00),
                new CashCardDto(101L, 150.00)
        );
    }

    @Test
    void cashCardSerializationTest() throws IOException {
        CashCardDto cashcard = this.cashCards[0];
        assertThat(json.write(cashcard)).isStrictlyEqualToJson("single.json");

        assertThat(json.write(cashcard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashcard)).hasJsonPathNumberValue("@.balance");

        assertThat(json.write(cashcard)).extractingJsonPathNumberValue("@.id").isEqualTo(22);
        assertThat(json.write(cashcard)).extractingJsonPathNumberValue("@.balance").isEqualTo(23.6);
    }

    @Test
    void cashCardDeserializationTest() throws IOException {
        String expected= """
                {
                  "id": 22,
                  "balance": 23.6
                }
                """;
        assertThat(json.parse(expected)).isEqualTo(this.cashCards[0]);
        assertThat(json.parseObject(expected).id()).isEqualTo(22);
        assertThat(json.parseObject(expected).balance()).isEqualTo(23.6);
    }

    @Test
    void cashCardListSerializationTest() throws IOException {
        assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");
    }

    @Test
    void cashCardListDeserializationTest() throws IOException {
        String expected= """
                [
                  {
                    "id": 22,
                    "balance": 23.6
                  },
                  {
                    "id": 100,
                    "balance": 1.00
                  },
                  {
                    "id": 101,
                    "balance": 150.00
                  }
                ]
                """;
        assertThat(jsonList.parse(expected)).isEqualTo(this.cashCards);
    }
}