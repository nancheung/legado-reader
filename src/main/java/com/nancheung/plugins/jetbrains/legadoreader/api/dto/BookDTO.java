package com.nancheung.plugins.jetbrains.legadoreader.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 书籍信息
 *
 * @author NanCheung
 */
@Data
public class BookDTO {
    @JsonProperty("author")
    private String author;
    @JsonProperty("bookUrl")
    private String bookUrl;
    @JsonProperty("canUpdate")
    private Boolean canUpdate;
    @JsonProperty("coverUrl")
    private String coverUrl;
    @JsonProperty("customCoverUrl")
    private String customCoverUrl;
    @JsonProperty("durChapterIndex")
    private Integer durChapterIndex;
    @JsonProperty("durChapterPos")
    private Integer durChapterPos;
    @JsonProperty("durChapterTime")
    private Long durChapterTime;
    @JsonProperty("durChapterTitle")
    private String durChapterTitle;
    @JsonProperty("group")
    private Integer group;
    @JsonProperty("intro")
    private String intro;
    @JsonProperty("kind")
    private String kind;
    @JsonProperty("lastCheckCount")
    private Integer lastCheckCount;
    @JsonProperty("lastCheckTime")
    private Long lastCheckTime;
    @JsonProperty("latestChapterTime")
    private Long latestChapterTime;
    @JsonProperty("latestChapterTitle")
    private String latestChapterTitle;
    @JsonProperty("name")
    private String name;
    @JsonProperty("order")
    private Integer order;
    @JsonProperty("origin")
    private String origin;
    @JsonProperty("originName")
    private String originName;
    @JsonProperty("originOrder")
    private Integer originOrder;
    @JsonProperty("readConfig")
    private ReadConfigDTO readConfig;
    @JsonProperty("tocUrl")
    private String tocUrl;
    @JsonProperty("totalChapterNum")
    private Integer totalChapterNum;
    @JsonProperty("type")
    private Integer type;
    @JsonProperty("wordCount")
    private String wordCount;
    @JsonProperty("variable")
    private String variable;
    @JsonProperty("customIntro")
    private String customIntro;
    
    @Data
    public static class ReadConfigDTO {
        @JsonProperty("delTag")
        private Integer delTag;
        @JsonProperty("imageStyle")
        private String imageStyle;
        @JsonProperty("reSegment")
        private Boolean reSegment;
        @JsonProperty("reverseToc")
        private Boolean reverseToc;
        @JsonProperty("splitLongChapter")
        private Boolean splitLongChapter;
    }
}
