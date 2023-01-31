package com.nancheung.plugins.jetbrains.legadoreader.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class R<D> {
    
    @JsonProperty("data")
    private D data;
    @JsonProperty("errorMsg")
    private String errorMsg;
    @JsonProperty("isSuccess")
    private Boolean isSuccess;
}
