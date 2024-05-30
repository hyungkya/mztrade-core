package com.mztrade.hki.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Comparator;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
@ToString @EqualsAndHashCode

@IdClass(StockPriceId.class)
@Table(name = "minute_stock_price")
@Entity
public class MinutelyStockPrice extends Bar {
    @Id
    @ManyToOne
    @JoinColumn(name = "ticker")
    private StockInfo stockInfo;
    @Id
    @Column
    private LocalDateTime date;
    @Column
    private Integer open;
    @Column
    private Integer high;
    @Column
    private Integer low;
    @Column
    private Integer close;
    @Column
    private Long volume;

    public static Comparator<MinutelyStockPrice> COMPARE_BY_DATE = new Comparator<MinutelyStockPrice>() {
        @Override
        public int compare(MinutelyStockPrice o1, MinutelyStockPrice o2) {
            return o1.getDate().compareTo(o2.getDate());
        }
    };
}
