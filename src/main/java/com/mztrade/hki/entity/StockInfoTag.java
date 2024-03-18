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
    @Id
    @JoinColumn(name = "tag")
    private int tid;
    @Id
    @JoinColumn(name = "stock_info")
    private String ticker;
}
