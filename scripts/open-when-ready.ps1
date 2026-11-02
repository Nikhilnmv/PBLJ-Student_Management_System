# Started by start.bat (Windows): waits until the application answers,
# then opens it in the default web browser. Gives up after about 20 minutes.
$url = 'http://localhost:8080/student-registration/'
for ($i = 0; $i -lt 600; $i++) {
    try {
        $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 3
        if ($response.StatusCode -eq 200) {
            Start-Process $url
            exit 0
        }
    } catch {
        # not ready yet - try again in 2 seconds
    }
    Start-Sleep -Seconds 2
}
