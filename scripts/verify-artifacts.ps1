# Test the artifact API endpoints (no auth needed now)
Write-Host "=== Testing GET /api/dashboard/artifacts/TC-AL-003 ==="
try {
    $resp = Invoke-RestMethod -Method Get -Uri 'http://localhost:8090/api/dashboard/artifacts/TC-AL-003'
    $resp | ConvertTo-Json -Depth 5
} catch {
    Write-Host "Error: $($_.Exception.Message)"
    Write-Host "Status: $($_.Exception.Response.StatusCode)"
}

Write-Host ""
Write-Host "=== Testing Screenshot URL directly ==="
try {
    $resp2 = Invoke-WebRequest -Method Get -Uri 'http://localhost:8090/api/dashboard/artifacts/TC-AL-003'
    $data = $resp2.Content | ConvertFrom-Json
    $ssUrl = $data.screenshotUrl
    $vidUrl = $data.videoUrl
    Write-Host "screenshotUrl: $ssUrl"
    Write-Host "videoUrl: $vidUrl"

    if ($ssUrl) {
        Write-Host ""
        Write-Host "=== Fetching screenshot file ==="
        try {
            $ssResp = Invoke-WebRequest -Method Get -Uri $ssUrl
            Write-Host "Screenshot HTTP status: $($ssResp.StatusCode)"
            Write-Host "Screenshot content-type: $($ssResp.Headers['Content-Type'])"
            Write-Host "Screenshot size: $($ssResp.RawContentLength) bytes"
        } catch {
            Write-Host "Screenshot fetch error: $($_.Exception.Message)"
        }
    }

    if ($vidUrl) {
        Write-Host ""
        Write-Host "=== Fetching video file ==="
        try {
            $vidResp = Invoke-WebRequest -Method Get -Uri $vidUrl
            Write-Host "Video HTTP status: $($vidResp.StatusCode)"
            Write-Host "Video content-type: $($vidResp.Headers['Content-Type'])"
            Write-Host "Video size: $($vidResp.RawContentLength) bytes"
        } catch {
            Write-Host "Video fetch error: $($_.Exception.Message)"
        }
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)"
}
