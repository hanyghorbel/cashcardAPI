package com.cashcard;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CASH_CARD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashCardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    double balance;
    @Column(nullable = false)
    String owner; //Important: improve this by making it its own entity.
}
