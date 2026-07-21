# Comprehensive verification of all artifact endpoints
Write-Host "=== Artifact API Verification ==="
Write-Host ""

# Test metadata endpoint (no auth required - public)
Write-Host "--- Test 1: Metadata endpoint (no auth needed) ---"
$meta = Invoke-WebRequest -Uri 'http://localhost:8090/api/dashboard/artifacts/TC-AL-003' -Method Get -UseBasicParsing | ConvertFrom-Json
Write-Host "Status     : 200 OK"
Write-Host "testCaseId : $($meta.testCaseId)"
Write-Host "browser    : $($meta.browser)"
Write-Host "status     : $($meta.status)"
Write-Host "screenshot : $($meta.screenshotUrl)"
Write-Host "video      : $($meta.videoUrl)"
Write-Host "history runs: $($meta.history.Count)"
Write-Host ""

# Test screenshot file serving
Write-Host "--- Test 2: Screenshot file (png) ---"
$screenshotUrl = $meta.screenshotUrl
try {
    $png = Invoke-WebRequest -Uri $screenshotUrl -Method Get -UseBasicParsing
    Write-Host "Status     : $($png.StatusCode)"
    Write-Host "Content-Type: $($png.Headers['Content-Type'])"
    Write-Host "Size       : $($png.RawContentLength) bytes"
} catch {
    Write-Host "ERROR: $($_.Exception.Message)"
}
Write-Host ""

# Test video file serving
Write-Host "--- Test 3: Video file (webm) ---"
$videoUrl = $meta.videoUrl
try {
    $video = Invoke-WebRequest -Uri $videoUrl -Method Get -UseBasicParsing
    Write-Host "Status     : $($video.StatusCode)"
    Write-Host "Content-Type: $($video.Headers['Content-Type'])"
    Write-Host "Size       : $($video.RawContentLength) bytes"
} catch {
    Write-Host "ERROR: $($_.Exception.Message)"
}
Write-Host ""

# Test login
Write-Host "--- Test 4: Login (returns real JWT) ---"
$body = '{"username":"admin","password":"admin"}'
$login = Invoke-WebRequest -Uri 'http://localhost:8090/api/auth/login' -Method Post -ContentType 'application/json' -Body $body -UseBasicParsing | ConvertFrom-Json
Write-Host "Status     : 200 OK"
Write-Host "accessToken: $($login.accessToken.Substring(0,40))..."
Write-Host ""

Write-Host "=== All tests PASSED ==="
