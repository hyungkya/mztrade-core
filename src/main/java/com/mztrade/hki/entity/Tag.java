package com.mztrade.hki.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
@ToString @EqualsAndHashCode

@Table(name = "tag")
@Entity
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

    @ManyToOne
    @JoinColumn(name = "uid")
    private User user;
}
