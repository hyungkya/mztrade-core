package com.mztrade.hki.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Comparator;
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

@IdClass(StockPriceId.class)
@Table(name = "stock_price")
@Entity
public class StockPrice {
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

    public static Comparator<StockPrice> COMPARE_BY_DATE = new Comparator<StockPrice>() {
        @Override
        public int compare(StockPrice o1, StockPrice o2) {
            return o1.getDate().compareTo(o2.getDate());
        }
    };
}
