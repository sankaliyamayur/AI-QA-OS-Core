---
name: locator-discovery
description: Skill for discovering robust web element locators (CSS, XPath, ARIA attributes, data-testid).
---

# Locator Discovery Skill

Guidelines for element locator strategy:

## Priority Order
1. `data-testid` or `data-cy` attributes (e.g. `[data-testid='login-button']`)
2. Unique ID attributes (e.g. `#email-input`)
3. Accessible ARIA attributes or roles (e.g. `role=button[name='Log in']`)
4. Name attributes (e.g. `[name='password']`)
5. Stable relative CSS selectors
6. **Avoid**: Brittle absolute XPaths (e.g. `/html/body/div[1]/div[2]/form/button[1]`)
