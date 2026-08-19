---
name: learning-analysis
description: Skill for analyzing historical failure trends, detecting recurring patterns, and updating learning memory.
---

# Learning Analysis Skill

Guidelines for pattern detection and learning updates:

## Analysis Scope
1. Aggregate historical execution runs, bug reports, and healing outcomes.
2. Identify recurring failure patterns (e.g. flaky network endpoints, unstable drawer animations).
3. Generate structured recommendations using HealingActionType enum values.
4. Record learning events with sourceAgent = "AI-QA-OS LearningAgent".
