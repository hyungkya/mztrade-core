package com.mztrade.hki.service;

import com.mztrade.hki.dto.StockInfoResponse;
import com.mztrade.hki.dto.TagResponse;
import com.mztrade.hki.entity.*;
import com.mztrade.hki.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    @Autowired
    private UserRepository userRepository;

    public TagService(TagRepositoryImpl tagRepositoryImpl) {
        this.tagRepositoryImpl = tagRepositoryImpl;
    }

    public List<TagResponse> getStockInfoTag(int uid) {
        List<TagResponse> tagResponses = tagRepository.findByUserUidAndCategory(uid, TagCategory.STOCK_INFO.id())
                .stream()
                .map((t) -> TagResponse.from(t))
                .toList();

        log.debug(String.format("getStockInfoTag(uid: %s) -> %s",uid, tagResponses));
        return tagResponses;
    }

    public List<TagResponse> getBacktestHistoryTag(int uid) {
        List<TagResponse> tagResponses = tagRepository.findByUserUidAndCategory(uid, TagCategory.BACKTEST_HISTORY.id())
                .stream()
                .map((t) -> TagResponse.from(t))
                .toList();

        log.debug(String.format("getBacktestHistoryTag(uid: %s) -> %s",uid, tagResponses));
        return tagResponses;
    }

    public List<TagResponse> getBacktestHistoryTagByAid(int uid, int aid) {
        List<TagResponse> tagResponses = tagRepositoryImpl.findByAid(uid, aid)
                .stream()
                .map((t) -> TagResponse.from(t))
                .toList();

        log.debug(String.format("getBacktestHistoryTagByAid(uid: %s,aid: %s) -> %s",uid, aid, tagResponses));
        return tagResponses;
    }

    public List<TagResponse> getStockInfoTagByTicker(Integer uid, String ticker) {
        List<TagResponse> tagResponses = tagRepositoryImpl.findByTicker(uid, ticker)
                .stream()
                .map((t) -> TagResponse.from(t))
                .toList();;

        log.debug(String.format("getStockInfoTagByTicker(uid: %s,ticker: %s) -> %s",uid, ticker, tagResponses));
        return tagResponses;
    }

    public int createTag(int uid,String name, String color, String category) {
        Tag tag = tagRepository.save(
                Tag.builder()
                        .user(userRepository.getReferenceById(uid))
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

    public List<StockInfoResponse> findStockInfoByNameAndTags(int uid, String name, List<Integer> tids) {
        List<StockInfoResponse> stockInfoResponses = tagRepositoryImpl.findStockInfoByNameAndTags(uid, name, tids)
                .stream()
                .map((s) -> StockInfoResponse.from(s))
                .toList();

        log.debug(String.format("findStockInfoByNameAndTags(uid: %d, name: %s, tids: %s) -> stockInfos: %b", uid, name, tids, stockInfoResponses));
        return stockInfoResponses;
    }


}
