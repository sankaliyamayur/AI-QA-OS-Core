package com.aiqaos.provider.contract;

import com.aiqaos.provider.model.LLMRequest;
import java.util.function.Consumer;

public interface StreamingLLMProvider {
    void stream(LLMRequest request, Consumer<String> tokenConsumer);
}