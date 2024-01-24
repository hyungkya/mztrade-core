package com.mztrade.hki.repository;

import com.mztrade.hki.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Boolean existsByName(String name);

    User save(User user);

    Optional<User> findByName(String name);

}
