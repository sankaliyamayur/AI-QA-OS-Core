package com.aiqaos.core.engine;

import com.aiqaos.core.contract.BrainRequest;
import com.aiqaos.core.contract.BrainResponse;

public interface QABrainEngine {
    BrainResponse processBrainDecision(BrainRequest request);
}
