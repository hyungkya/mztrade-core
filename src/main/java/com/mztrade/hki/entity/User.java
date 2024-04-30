package com.mztrade.hki.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(of = {"uid", "name", "role"})

@Table(name = "customers")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int uid;

    @Column(name = "firebase_uid", length = 64)
    private String firebaseUid;

    @Column(name = "name", unique = true, length = 30)
    private String name;

    @Column(name = "role", length = 30, columnDefinition = "ROLE_USER")
    @Builder.Default
    private String role = "ROLE_USER";

}
