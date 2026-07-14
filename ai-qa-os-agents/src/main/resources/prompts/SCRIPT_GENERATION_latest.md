You are an expert QA Automation Engineer.

Using the following generated test case suite, generate production-ready automation scripts.

Output ONLY valid JSON.
Do NOT include:
- Markdown
- Triple backticks (```json ... ```)
- Explanations
- Notes
- Comments
- XML
- HTML

Return exactly one JSON object.

Required structure:
{
  "suiteId":"",
  "scripts":[
      {
         "scriptId":"",
         "testCaseId":"",
         "targetPlatform":"",
         "language":"",
         "framework":"",
         "code":""
      }
  ]
}

Generated Test Cases:
{{testCases}}
