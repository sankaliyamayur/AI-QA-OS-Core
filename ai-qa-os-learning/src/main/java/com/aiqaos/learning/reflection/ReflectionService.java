package com.aiqaos.learning.reflection;

import com.aiqaos.core.model.FailurePattern;
import java.util.List;

/**
 * LRN-1: the loop's improvement stage — reflect over root-caused {@link FailurePattern}s and produce
 * concrete, typed {@link ImprovementProposal}s. Implemented by {@link DefaultReflectionService}.
 */
public interface ReflectionService {

    ReflectionResult reflect(List<FailurePattern> patterns);
}
