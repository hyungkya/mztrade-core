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

    public List<Tag> getBacktestHistoryTagByAid(int uid, int aid) {
        List<Tag> tags = tagRepository.findByAid(uid, aid);

        log.debug(String.format("getBacktestHistoryTagByAid(uid: %s,aid: %s) -> %s",uid, aid, tags));
        return tags;
    }

    public List<Tag> getStockInfoTagByTicker(Integer uid, String ticker) {
        List<Tag> tags = tagRepository.findByTicker(uid, ticker);

        log.debug(String.format("getStockInfoTagByTicker(uid: %s,ticker: %s) -> %s",uid, ticker, tags));
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

    public boolean createBacktestHistoryTagLink(int tid, int aid) {
        boolean isProcessed = tagRepository.createBacktestHistoryTagLink(tid, aid);

        log.debug(String.format("createBacktestHistoryTagLink(tid: %d, aid: %d) -> isProcessed: %b", tid, aid, isProcessed));
        return isProcessed;
    }

    public boolean deleteBacktestHistoryTagLink(int tid, int aid) {
        boolean isProcessed = tagRepository.deleteBacktestHistoryTagLink(tid, aid);

        log.debug(String.format("deleteBacktestHistoryTagLink(tid: %d, aid: %d) -> isProcessed: %b", tid, aid, isProcessed));
        return isProcessed;
    }

    public boolean createStockInfoTagLink(int tid, String ticker) {
        boolean isProcessed = tagRepository.createStockInfoTagLink(tid, ticker);

        log.debug(String.format("createStockInfoTagLink(tid: %d, ticker: %s) -> isProcessed: %b", tid, ticker, isProcessed));
        return isProcessed;
    }

    public boolean deleteStockInfoTagLink(int tid, String ticker) {
        boolean isProcessed = tagRepository.deleteStockInfoTagLink(tid, ticker);

        log.debug(String.format("deleteStockInfoTagLink(tid: %d, ticker: %s) -> isProcessed: %b", tid, ticker, isProcessed));
        return isProcessed;
    }
}