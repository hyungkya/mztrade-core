package com.mztrade.hki.dto;

import com.mztrade.hki.entity.Tag;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class TagResponse {
    private int tid;
    private int uid;
    private String tname;
    private String tcolor;
    private int category;
    static public TagResponse from(Tag tag) {
        return TagResponse.builder()
                .tid(tag.getTid())
                .uid(tag.getUser().getUid())
                .tname(tag.getTname())
                .tcolor(tag.getTcolor())
                .category(tag.getCategory())
                .build();
    }
}
