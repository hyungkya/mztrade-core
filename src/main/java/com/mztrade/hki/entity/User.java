package com.mztrade.hki.entity;

import jakarta.persistence.*;

import java.util.*;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
