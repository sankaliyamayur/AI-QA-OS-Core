package com.aiqaos.core.contract;

public class PromptResponse extends BaseResponse {
    private String renderedContent;

    public String getRenderedContent() { return renderedContent; }
    public void setRenderedContent(String renderedContent) { this.renderedContent = renderedContent; }
}