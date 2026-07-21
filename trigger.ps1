$body = @{
    correlationId = "corr-us-003"
    userId = "tester-1"
    workflowName = "AUTONOMOUS_QA_PIPELINE"
    parameters = @{
        requirementPath = "d:/QA AI Automation/AI-QA-OS Architecture/AI-QA-OS-Core/resources/user-stories/Login/US-001.md"
    }
} | ConvertTo-Json -Depth 5

Write-Host "Sending request body:"
Write-Host $body

$response = Invoke-RestMethod -Method Post -Uri "http://localhost:8082/api/v1/workflows/start" -ContentType "application/json" -Body $body
$response | ConvertTo-Json -Depth 5
