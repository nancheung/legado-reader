package com.nancheung.plugins.jetbrains.legadoreader.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BookChapterDTO {
    
    @JsonProperty("baseUrl")
    private String baseUrl;
    @JsonProperty("bookUrl")
    private String bookUrl;
    @JsonProperty("index")
    private Integer index;
    @JsonProperty("isPay")
    private Boolean isPay;
    @JsonProperty("isVip")
    private Boolean isVip;
    @JsonProperty("isVolume")
    private Boolean isVolume;
    @JsonProperty("tag")
    private String tag;
    @JsonProperty("title")
    private String title;
    @JsonProperty("url")
    private String url;
}
