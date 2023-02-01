package com.nancheung.plugins.jetbrains.legadoreader.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 书籍信息
 *
 * @author NanCheung
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookProgressDTO {
    
    @JsonProperty("name")
    private String name;
    @JsonProperty("author")
    private String author;
    @JsonProperty("durChapterIndex")
    private Integer durChapterIndex;
    @JsonProperty("durChapterPos")
    private Integer durChapterPos;
    @JsonProperty("durChapterTime")
    private Long durChapterTime;
    @JsonProperty("durChapterTitle")
    private String durChapterTitle;
}
