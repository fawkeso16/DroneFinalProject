Start-Process powershell -ArgumentList '-NoExit', '-Command', 'cd backend\Drone-Project\Drone-Project; .\mvnw.cmd spring-boot:run'
Start-Sleep -Seconds 5
Start-Process powershell -ArgumentList '-NoExit', '-Command', 'cd frontend\dfront; npm start'
