package com.mztrade.hki.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
