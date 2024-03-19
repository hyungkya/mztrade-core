package com.mztrade.hki.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@Entity
@NoArgsConstructor
@IdClass(StockInfoTagId.class)
@Table(name = "stock_info_tag")
@ToString
@AllArgsConstructor
public class StockInfoTag {
    @Id @ManyToOne
    @JoinColumn(name = "tid")
    private Tag tag;
    @Id @ManyToOne
    @JoinColumn(name = "ticker")
    private StockInfo stockInfo;
}
