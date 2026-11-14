package com.cashcard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CashCardJsonTest {

    @Autowired
    private JacksonTester<CashCard> json;

    @Test
    void myFirst() {
        //IMPORTANT: for testing we use AssertJ
        assertThat(1).isEqualTo(1);
    }

    @Test
    void CashCardSerializationTest() throws IOException {
        CashCard cashcard = new CashCard(22L,23.6);
        assertThat(json.write(cashcard)).isStrictlyEqualToJson("expected.json");

        assertThat(json.write(cashcard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashcard)).hasJsonPathNumberValue("@.balance");

        assertThat(json.write(cashcard)).extractingJsonPathNumberValue("@.id").isEqualTo(22);
        assertThat(json.write(cashcard)).extractingJsonPathNumberValue("@.balance").isEqualTo(23.6);
    }

    @Test
    void CashCardDeserializationTest() throws IOException {
        String expected= """
                {
                  "id": 22,
                  "balance": 23.6
                }
                """;
        assertThat(json.parse(expected)).isEqualTo(new CashCard(22L,23.6));
        assertThat(json.parseObject(expected).id()).isEqualTo(22);
        assertThat(json.parseObject(expected).balance()).isEqualTo(23.6);
    }
}