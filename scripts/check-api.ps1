# Login
$loginBody = @{ username = 'admin'; password = 'admin' } | ConvertTo-Json
try {
    $loginResp = Invoke-RestMethod -Method Post -Uri 'http://localhost:8090/api/auth/login' -ContentType 'application/json' -Body $loginBody
    $token = $loginResp.accessToken
    Write-Host "Token obtained: $($token.Substring(0, [Math]::Min(30, $token.Length)))..."
} catch {
    Write-Host "Login failed: $_"
    # Try without auth
    $token = $null
}

# Try artifacts API with auth
$headers = @{ Authorization = "Bearer $token" }

Write-Host ""
Write-Host "=== Testing artifact API endpoints ==="

# Try different possible endpoints
$endpoints = @(
    "http://localhost:8090/api/dashboard/artifacts/TC-AL-003",
    "http://localhost:8090/api/dashboard/artifacts/TC-AL-003/history",
    "http://localhost:8090/api/artifacts/exec-841076ca/chromium/test-results/TC-AL-003-AC-003-Verify-Login-Failure-with-Invalid-Password-chromium/test-failed-1.png"
)

foreach ($url in $endpoints) {
    Write-Host ""
    Write-Host "--- GET $url ---"
    try {
        if ($token) {
            $resp = Invoke-RestMethod -Method Get -Uri $url -Headers $headers
        } else {
            $resp = Invoke-RestMethod -Method Get -Uri $url
        }
        $resp | ConvertTo-Json -Depth 5
    } catch {
        Write-Host "Error: $($_.Exception.Message)"
        # Try to get response body
        try {
            $body = $_.ErrorDetails.Message
            Write-Host "Response body: $body"
        } catch {}
    }
}
