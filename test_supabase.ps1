# Test Supabase Connection
# Replace YOUR_CORRECT_ANON_KEY with your actual anon key from Supabase dashboard

$url = "https://dgyonbbyifaqsffbzdpk.supabase.co/rest/v1/ships?select=*&limit=5"
$apiKey = "YOUR_CORRECT_ANON_KEY_HERE"  # Get this from Supabase Dashboard > Settings > API

$headers = @{
    "apikey" = $apiKey
    "Authorization" = "Bearer $apiKey"
    "Content-Type" = "application/json"
}

Write-Host "Testing Supabase connection..." -ForegroundColor Yellow
Write-Host "URL: $url" -ForegroundColor Cyan

try {
    $response = Invoke-WebRequest -Uri $url -Headers $headers -Method GET
    Write-Host "`nConnection SUCCESS!" -ForegroundColor Green
    Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "`nData retrieved:" -ForegroundColor Cyan
    $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3
}
catch {
    Write-Host "`nConnection FAILED!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "Error details: $errorBody" -ForegroundColor Red
    }
}

Write-Host "`n--- Test Add Ship ---" -ForegroundColor Yellow
$newShip = @{
    id = [guid]::NewGuid().ToString()
    name = "Test Ship " + (Get-Date -Format "HHmmss")
    capacity = 100
    description = "Test ship from PowerShell"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "https://dgyonbbyifaqsffbzdpk.supabase.co/rest/v1/ships" -Headers $headers -Method POST -Body $newShip
    Write-Host "Add Ship SUCCESS!" -ForegroundColor Green
    Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
    $response.Content
}
catch {
    Write-Host "Add Ship FAILED!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
