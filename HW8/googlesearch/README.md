# Googlesearch (HW8)

簡短說明：這是一個簡單的 Spring Boot 應用，透過 Google Custom Search 或直接解析 Google 搜尋結果，顯示搜尋標題與連結。

快速開始

1. 設定 Java
   - 需要 JDK 17 或以上（本機已使用 JDK 25 測試）。

2. 設定 Google API
   - 在 `src/main/resources/application.properties` 中，設定：
     ```properties
     google.cse.enabled=true
     google.cse.apiKey=你的API金鑰
     google.cse.cx=你的搜尋引擎ID
     ```
   - 範例檔：`src/main/resources/application.properties.example`（請參考並填入你的金鑰）

3. 執行（開發）
   ```powershell
   Set-Location 'C:\Users\Tsai\Desktop\HW\2025Fall-DS\HW8\googlesearch'
   .\mvnw.cmd spring-boot:run
   # 或背景執行並把日誌導到 logs\app.log
   Start-Process -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run" -NoNewWindow -RedirectStandardOutput ".\logs\app.log" -RedirectStandardError ".\logs\app.err.log"
   Get-Content .\logs\app.log -Wait -Tail 50
   ```

4. 開啟瀏覽器
   - 訪問 `http://localhost:8080` 進行搜尋測試。

注意事項
- 請勿將實際的 API 金鑰公開到公開倉庫。建議將 `src/main/resources/application.properties` 加入 `.gitignore`，或在 CI/部署時以環境變數注入。
- 本專案附有 `mvnw`（Maven Wrapper），不需要系統全域安裝 Maven。

繳交
- 上傳 GitHub 倉庫連結與搜尋結果截圖到 Moodle。