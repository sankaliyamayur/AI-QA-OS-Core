package com.aiqaos.learning.dashboard;

/**
 * LRN-3: the at-a-glance health of the learning loop — {@code HEALTHY} when it is improving or
 * holding with a good score, {@code AT_RISK} when it is regressing or the score is low (the signal
 * that makes autonomous learning governable, and the hook for a stop-loss).
 */
public enum LearningHealth {
    HEALTHY,
    AT_RISK
}
