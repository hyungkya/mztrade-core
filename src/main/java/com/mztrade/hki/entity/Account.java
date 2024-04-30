package com.mztrade.hki.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
@ToString @EqualsAndHashCode

@Table(name = "account")
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int aid;
    @ManyToOne
    @JoinColumn(name = "uid")
    private User user;
    @Column(nullable = false, columnDefinition = "0")
    private long balance;
    @Column(length = 16, nullable = false, columnDefinition = "BACKTEST")
    @Builder.Default
    private String type = "BACKTEST";
    @OneToMany(mappedBy = "account")
    private Set<Position> positions = new HashSet<>();
    @OneToMany(mappedBy = "account")
    private Set<Order> orders = new HashSet<>();
}
