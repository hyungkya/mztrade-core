package com.mztrade.hki.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@ToString
@Entity
@Table(name = "account_history")
@IdClass(AccountHistoryId.class)
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistory {
    @Id
    @JoinColumn(table = "account", referencedColumnName = "aid")
    private int aid;
    @Id
    @Column(nullable = false)
    private LocalDateTime date;
    @Column(nullable = false, columnDefinition = "0")
    private long balance;
}
