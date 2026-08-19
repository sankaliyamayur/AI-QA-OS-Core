---
name: test-case-generation
description: Skill for generating structured test cases with executable steps, expected results, and priorities based on QA Analysis.
---

# Test Case Generation Skill

Guidelines for test case generation:

## Test Case Design Rules
1. Assign sequential IDs (`TC-001`, `TC-002`, etc.).
2. Derive test cases directly from identified scenarios and Acceptance Criteria.
3. Steps must be clear, sequential, browser-executable actions (e.g., "Navigate to https://onepurpos.in/openings", "Click Login dropdown", "Enter valid email in Email field").
4. Priority Assignment:
   - **HIGH**: Core functionality (valid login, session creation, password recovery navigation).
   - **MEDIUM**: Input validation (invalid format, blank fields, incorrect password).
   - **LOW**: Secondary boundary conditions.
5. Provide unambiguous `expectedResult` statements for each test case.
