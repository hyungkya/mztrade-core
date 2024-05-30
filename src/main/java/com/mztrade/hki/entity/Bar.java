package com.mztrade.hki.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Comparator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Bar {
    private LocalDateTime date;
    private Integer open;
    private Integer high;
    private Integer low;
    private Integer close;
    private Long volume;
    public static Comparator<Bar> COMPARE_BY_DATE = new Comparator<Bar>() {
        @Override
        public int compare(Bar o1, Bar o2) {
            return o1.getDate().compareTo(o2.getDate());
        }
    };
}
