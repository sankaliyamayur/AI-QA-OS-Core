package com.aiqaos.learning.metrics;

/**
 * LRN-2: the direction of the learning loop's performance over the observed window — whether the
 * platform is measurably getting better, holding steady, or regressing (the signal LRN-4 can use as
 * a stop-loss).
 */
public enum LearningTrend {
    IMPROVING,
    STABLE,
    REGRESSING
}
