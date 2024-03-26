package com.mztrade.hki.repository;


import com.mztrade.hki.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Tag save(Tag tag);
    List<Tag> findByUserUidAndCategory(Integer userId, Integer category);
    Optional<Tag> findByUserUidAndTname(Integer userId, String tagName);
}
