package com.mztrade.hki.repository;


import com.mztrade.hki.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Tag save(Tag tag);
    List<Tag> findByUserUidAndCategory(Integer userId, Integer category);

}
