package com.mztrade.hki.controller;

import com.mztrade.hki.dto.TagResponse;
import com.mztrade.hki.entity.TagCategory;
import com.mztrade.hki.entity.TagRequest;
import com.mztrade.hki.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
public class TagController {

    private TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/tag")
    public ResponseEntity<List<TagResponse>> findTags(
            @RequestParam Integer uid,
            @RequestParam String category,
            @RequestParam Optional<String> ticker,
            @RequestParam Optional<Integer> aid
    ) {
        log.info(String.format("[GET] /tag?uid=%s&category=%s", uid, category));
        TagCategory tagCategory;
        try {
            tagCategory = TagCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            //TODO::카테고리를 찾을 수 없습니다.
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        //TODO::API 리스트 반환하는 친구들 중 null로 할지 []로 할지 정하기
        List<TagResponse> tagResponses = null;
        if (tagCategory.equals(TagCategory.STOCK_INFO)) {
            if (ticker.isPresent()) {
                tagResponses = tagService.getStockInfoTagByTicker(uid, ticker.get());
            } else {
                tagResponses = tagService.getStockInfoTag(uid);
            }
            //TODO::BACKTEST_HISTORY 수정 -> RESULTS
        } else if (tagCategory.equals(TagCategory.BACKTEST_HISTORY)) {
            if (aid.isPresent()) {
                tagResponses = tagService.getBacktestResultTagByAid(uid, aid.get());
            } else {
                tagResponses = tagService.getBacktestResultTag(uid);
            }
        }
        return new ResponseEntity<>(tagResponses, HttpStatus.OK);
    }

    @PostMapping("/stock_info/tag-link")
    public ResponseEntity<Boolean> createStockInfoTagLink(@RequestParam Integer tid,
                                                          @RequestParam String ticker) {
        Boolean isProcessed = tagService.createStockInfoTagLink(tid, ticker);
        log.info(String.format("[POST] /stock_info/tag-link/tid=%s&ticker=%s", tid, ticker));
        return new ResponseEntity<>(isProcessed, HttpStatus.OK);
    }

    @DeleteMapping("/stock_info/tag-link")
    public ResponseEntity<Boolean> deleteStockInfoTagLink(@RequestParam Integer tid,
                                                          @RequestParam String ticker) {
        tagService.deleteStockInfoTagLink(tid, ticker);
        log.info(String.format("[DELETE] /stock_info/tag-link/tid=%s&ticker=%s", tid, ticker));
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PostMapping("/backtest/tag-link")
    public ResponseEntity<Boolean> createBacktestResultTagLink(@RequestParam Integer tid,
                                                               @RequestParam Integer aid) {
        Boolean isProcessed = tagService.createBacktestResultTagLink(tid, aid);
        log.info(String.format("[POST] /backtest/tag-link/tid=%s&aid=%s", tid, aid));
        return new ResponseEntity<>(isProcessed, HttpStatus.OK);
    }

    @DeleteMapping("/backtest/tag-link")
    public ResponseEntity<Boolean> deleteBacktestResultTagLink(@RequestParam Integer tid,
                                                               @RequestParam Integer aid) {
        tagService.deleteBacktestResultTagLink(tid, aid);
        log.info(String.format("[DELETE] /backtest/tag-link/tid=%s&aid=%s", tid, aid));
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PostMapping("/tag")
    public boolean createTag(@RequestBody TagRequest tagRequest) {
        int create = tagService.createTag(tagRequest.getUid(), tagRequest.getName(),
                tagRequest.getColor(), tagRequest.getCategory());
        log.info(String.format("[POST] /tag tag=%s", tagRequest));
        return create > 0;
    }

    @DeleteMapping("/tag")
    public ResponseEntity<Boolean> deleteTag(@RequestParam Integer tid) {
        tagService.deleteTag(tid);
        log.info(String.format("[DELETE] /tag/tid=%s", tid));
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PutMapping("/tag")
    public boolean updateTag(@RequestParam Integer tid, @RequestParam String name,
                             @RequestParam String color) {
        boolean update = tagService.updateTag(tid, name, color);
        log.info(String.format("[PUT] /tag/tid=%s&name=%s&color=%s", tid, name, color));
        return update;
    }
}
