package com.mztrade.hki.service;

import com.mztrade.hki.dto.StockInfoResponse;
import com.mztrade.hki.dto.TagResponse;
import com.mztrade.hki.entity.BacktestResultTag;
import com.mztrade.hki.entity.StockInfoTag;
import com.mztrade.hki.entity.Tag;
import com.mztrade.hki.entity.TagCategory;
import com.mztrade.hki.repository.AccountRepository;
import com.mztrade.hki.repository.BacktestResultTagRepository;
import com.mztrade.hki.repository.StockInfoRepository;
import com.mztrade.hki.repository.StockInfoTagRepository;
import com.mztrade.hki.repository.TagRepository;
import com.mztrade.hki.repository.TagRepositoryImpl;
import com.mztrade.hki.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private BacktestResultTagRepository backtestResultTagRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StockInfoRepository stockInfoRepository;
    @Autowired
    private AccountRepository accountRepository;

    public TagService(TagRepositoryImpl tagRepositoryImpl) {
        this.tagRepositoryImpl = tagRepositoryImpl;
    }

    public Optional<TagResponse> findTagByName(int uid, String name) {
        Optional<TagResponse> tagResponse = Optional.empty();
        Optional<Tag> tag = tagRepository.findByUserUidAndTname(uid, name);
        if (tag.isPresent()) {
            tagResponse = Optional.of(TagResponse.from(tagRepository.findByUserUidAndTname(uid, name).get()));
        }
        return tagResponse;
    }

    public int createIfNotExists(int uid, String name, TagCategory category) {
        Optional<TagResponse> tagResponse = findTagByName(uid, name);
        int tid;
        if (tagResponse.isEmpty()) {
            tid = createTag(uid, name, "0xFFFFFFFF", category.name());
        } else {
            tid = tagResponse.get().getTid();
        }
        return tid;
    }

    public List<TagResponse> getStockInfoTag(int uid) {
        List<TagResponse> tagResponses = tagRepository.findByUserUidAndCategory(uid, TagCategory.STOCK_INFO.id())
                .stream()
                .map((t) -> TagResponse.from(t))
                .toList();

        return tagResponses;
    }

    public List<TagResponse> getBacktestResultTag(int uid) {
        List<TagResponse> tagResponses = tagRepository.findByUserUidAndCategory(uid, TagCategory.BACKTEST_HISTORY.id())
                .stream()
                .map((t) -> TagResponse.from(t))
                .toList();

        return tagResponses;
    }

    public List<TagResponse> getBacktestResultTagByAid(int uid, int aid) {
        List<TagResponse> tagResponses = backtestResultTagRepository.findByTagUserUidAndAccountAid(uid, aid)
                .stream()
                .map((t) -> TagResponse.from(t.getTag()))
                .toList();

        return tagResponses;
    }

    public List<TagResponse> getStockInfoTagByTicker(Integer uid, String ticker) {
        List<TagResponse> tagResponses = stockInfoTagRepository.findByTagUserUidAndStockInfoTicker(uid, ticker)
                .stream()
                .map((t) -> TagResponse.from(t.getTag()))
                .toList();;

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

        return tag.getTid();
    }

    public void deleteTag(int tid) {
        tagRepository.deleteById(tid);
    }

    public boolean updateTag(int tid,String name, String color) {
        Tag tag = tagRepository.getReferenceById(tid);
        tag.setTname(name);
        tag.setTcolor(color);
        tagRepository.save(tag);
        return true;
    }

    public boolean createBacktestResultTagLink(int tid, int aid) {
        backtestResultTagRepository.save(
                BacktestResultTag.builder()
                        .tag(tagRepository.getReferenceById(tid))
                        .account(accountRepository.getReferenceById(aid))
                        .build()
        );
        return true;
    }

    public void deleteBacktestResultTagLink(int tid, int aid) {
        backtestResultTagRepository.delete(
                BacktestResultTag.builder()
                        .tag(tagRepository.getReferenceById(tid))
                        .account(accountRepository.getReferenceById(aid))
                        .build()
        );
    }

    public boolean createStockInfoTagLink(int tid, String ticker) {
        stockInfoTagRepository.save(
                StockInfoTag.builder()
                .tag(tagRepository.getReferenceById(tid))
                .stockInfo(stockInfoRepository.getByTicker(ticker))
                .build()
        );

        return true;
    }

    public void deleteStockInfoTagLink(int tid, String ticker) {
        stockInfoTagRepository.delete(
                StockInfoTag.builder()
                        .tag(tagRepository.getReferenceById(tid))
                        .stockInfo(stockInfoRepository.getByTicker(ticker))
                        .build()
        );

    }

    public List<StockInfoResponse> findStockInfoByNameAndTags(int uid, String name, List<Integer> tids) {
        List<StockInfoResponse> stockInfoResponses = tagRepositoryImpl.findStockInfoByNameAndTags(uid, name, tids)
                .stream()
                .map((s) -> StockInfoResponse.from(s))
                .toList();

        return stockInfoResponses;
    }


}
