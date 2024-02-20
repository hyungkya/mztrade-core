package com.mztrade.hki.service;

import com.mztrade.hki.entity.Tag;
import com.mztrade.hki.entity.TagCategory;
import com.mztrade.hki.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TagService {
    @Autowired
    private TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> getStockInfoTag(int uid) {
        List<Tag> tags = tagRepository.findByCategory(uid, TagCategory.STOCK_INFO);

        log.debug(String.format("getStockInfoTag(uid: %s) -> %s",uid, tags));
        return tags;
    }

    public List<Tag> getBacktestHistoryTag(int uid) {
        List<Tag> tags = tagRepository.findByCategory(uid, TagCategory.BACKTEST_HISTORY);

        log.debug(String.format("getBacktestHistoryTag(uid: %s) -> %s",uid, tags));
        return tags;
    }

    public int createTag(int uid,String name, String color, String category) {
        int tid = tagRepository.createTag(uid,name,color, TagCategory.valueOf(category));

        log.debug(String.format("createTag(uid: %s, color: %s, category: %s) -> tid:%s",uid,color,category,tid));
        return tid;
    }

    public boolean deleteTag(int tid) {
        boolean delete = tagRepository.deleteById(tid);
        log.debug(String.format("deleteTag(tid: %s) -> delete:%s",tid,delete));
        return delete;
    }



}
