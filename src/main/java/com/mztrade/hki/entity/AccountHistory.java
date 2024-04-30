package com.mztrade.hki.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
