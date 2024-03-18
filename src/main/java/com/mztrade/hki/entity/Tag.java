package com.mztrade.hki.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@Entity
@NoArgsConstructor
@Table(name = "tag")
@ToString
@AllArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int tid;

    @Column(name = "tname", length = 16, nullable = false)
    private String tname;

    @Column(name = "tcolor", length = 10, nullable = false)
    private String tcolor;

    @Column(name = "category", nullable = false)
    private int category;

    @JoinColumn(name="uid", referencedColumnName = "customers")
    private int uid;
}
