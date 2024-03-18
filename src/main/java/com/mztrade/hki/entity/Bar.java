package com.mztrade.hki.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

@Getter
@Setter
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
@Table(name = "stock_price")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Bar {
    @Id
    @JoinColumn(table = "stock_info", referencedColumnName = "ticker")
    private String ticker;
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

    public static Comparator<Bar> COMPARE_BY_DATE = new Comparator<Bar>() {
        @Override
        public int compare(Bar o1, Bar o2) {
            return o1.getDate().compareTo(o2.getDate());
        }
    };
}
