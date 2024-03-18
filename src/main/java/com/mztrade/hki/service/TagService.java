package com.mztrade.hki.service;

import com.mztrade.hki.entity.*;
import com.mztrade.hki.repository.BacktestHistoryTagRepository;
import com.mztrade.hki.repository.StockInfoTagRepository;
import com.mztrade.hki.repository.TagRepository;
import com.mztrade.hki.repository.TagRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TagService {
    @Autowired
    private TagRepositoryImpl tagRepositoryImpl;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private StockInfoTagRepository stockInfoTagRepository;
    @Autowired
    private BacktestHistoryTagRepository backtestHistoryTagRepository;

    public TagService(TagRepositoryImpl tagRepositoryImpl) {
        this.tagRepositoryImpl = tagRepositoryImpl;
    }

    public List<Tag> getStockInfoTag(int uid) {
        List<Tag> tags = tagRepositoryImpl.findByCategory(uid, TagCategory.STOCK_INFO);

        log.debug(String.format("getStockInfoTag(uid: %s) -> %s",uid, tags));
        return tags;
    }

    public List<Tag> getBacktestHistoryTag(int uid) {
        List<Tag> tags = tagRepositoryImpl.findByCategory(uid, TagCategory.BACKTEST_HISTORY);

        log.debug(String.format("getBacktestHistoryTag(uid: %s) -> %s",uid, tags));
        return tags;
    }

    public List<Tag> getBacktestHistoryTagByAid(int uid, int aid) {
        List<Tag> tags = tagRepositoryImpl.findByAid(uid, aid);

        log.debug(String.format("getBacktestHistoryTagByAid(uid: %s,aid: %s) -> %s",uid, aid, tags));
        return tags;
    }

    public List<Tag> getStockInfoTagByTicker(Integer uid, String ticker) {
        List<Tag> tags = tagRepositoryImpl.findByTicker(uid, ticker);

        log.debug(String.format("getStockInfoTagByTicker(uid: %s,ticker: %s) -> %s",uid, ticker, tags));
        return tags;
    }

    public int createTag(int uid,String name, String color, String category) {
        Tag tag = tagRepository.save(
                Tag.builder()
                        .uid(uid)
                        .tname(name)
                        .tcolor(color)
                        .category(TagCategory.valueOf(category).id())
                        .build());

        log.debug(String.format("createTag(uid: %s, color: %s, category: %s) -> tid:%s",uid,color,category,tag.getTid()));
        return tag.getTid();
    }

    public void deleteTag(int tid) {
        tagRepository.deleteById(tid);
        log.debug(String.format("deleteTag(tid: %s)",tid));
    }

    public boolean updateTag(int tid,String name, String color) {
        Tag tag = tagRepository.getReferenceById(tid);
        tag.setTname(name);
        tag.setTcolor(color);
        tagRepository.save(tag);
        log.debug(String.format("updateTag(tid: %s, name: %s, color: %s) -> update:%s",tid,name,color, true));
        return true;
    }

    public boolean createBacktestHistoryTagLink(int tid, int aid) {
        backtestHistoryTagRepository.save(BacktestHistoryTag.builder().tid(tid).aid(aid).build());

        log.debug(String.format("createBacktestHistoryTagLink(tid: %d, aid: %d) -> isProcessed: %b", tid, aid, true));
        return true;
    }

    public void deleteBacktestHistoryTagLink(int tid, int aid) {
        backtestHistoryTagRepository.delete(BacktestHistoryTag.builder().tid(tid).aid(aid).build());

        log.debug(String.format("deleteBacktestHistoryTagLink(tid: %d, aid: %d)", tid, aid));
    }

    public boolean createStockInfoTagLink(int tid, String ticker) {
        stockInfoTagRepository.save(StockInfoTag.builder().tid(tid).ticker(ticker).build());

        log.debug(String.format("createStockInfoTagLink(tid: %d, ticker: %s) -> isProcessed: %b", tid, ticker, true));
        return true;
    }

    public void deleteStockInfoTagLink(int tid, String ticker) {
        stockInfoTagRepository.delete(StockInfoTag.builder().tid(tid).ticker(ticker).build());

        log.debug(String.format("deleteStockInfoTagLink(tid: %d, ticker: %s)", tid, ticker));
    }

    public List<StockInfo> findStockInfoByNameAndTags(int uid, String name, List<Integer> tids) {
        List<StockInfo> stockInfos = tagRepositoryImpl.findStockInfoByNameAndTags(uid, name, tids);

        log.debug(String.format("findStockInfoByNameAndTags(uid: %d, name: %s, tids: %s) -> stockInfos: %b", uid, name, tids, stockInfos));
        return stockInfos;
    }


}
