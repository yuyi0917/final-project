package com.example.googlesearch.service;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TranslatorBridge {

    // 您的 GAS 網址
    private static final String GAS_URL = "https://script.google.com/macros/s/AKfycby8ngXqlohnz4JOT8YCzZmzXsNImQ_Iri-9DvRXAYbe-HIgw8YLTXHOCC6_m_2NW37WKA/exec";

    private final HttpClient client;

    public TranslatorBridge() {
        // ★★★ 修正 1: 設定自動跟隨轉址 (ALWAYS) ★★★
        // Google Script 必定會轉址，設為 NORMAL 自動處理最穩當
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String translateToEnglish(String originalText) {
        if (originalText == null || originalText.trim().isEmpty()) return "";
        
        // 如果輸入已經是純英數字，直接回傳 (省時間)
        if (originalText.matches("[a-zA-Z0-9\\s\\p{Punct}]+")) {
            return originalText;
        }

        try {
            System.out.println("============== 翻譯開始 ==============");
            System.out.println("原始關鍵字: " + originalText);

            String encodedText = URLEncoder.encode(originalText, StandardCharsets.UTF_8);
            String finalUrl = GAS_URL + "?q=" + encodedText;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(finalUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // ★★★ 修正 2: 印出回傳內容，方便除錯 ★★★
            String jsonResponse = response.body();
            System.out.println("API 回傳狀態碼: " + response.statusCode());
            // System.out.println("API 回傳內容: " + jsonResponse); // 如果內容太長可註解掉

            if (response.statusCode() != 200) {
                System.err.println("翻譯 API 連線異常，狀態碼非 200");
                return originalText;
            }

            // ★★★ 修正 3: 使用 Regex 解析 JSON，比 indexOf 更穩定 ★★★
            // 尋找 "translated":"內容"
            Pattern pattern = Pattern.compile("\"translated\":\"(.*?)\"");
            Matcher matcher = pattern.matcher(jsonResponse);

            if (matcher.find()) {
                String translated = matcher.group(1);
                // 處理 unicode 脫逸字元 (簡單處理)
                translated = decodeUnicode(translated);
                System.out.println("✅ 翻譯成功: " + translated);
                System.out.println("======================================");
                return translated;
            } else {
                System.err.println("❌ JSON 解析失敗，找不到 translated 欄位。");
                // 如果回傳的是 HTML (例如 Google 登入頁)，這裡就會印出來提醒您權限設錯了
                if (jsonResponse.contains("<!DOCTYPE html>")) {
                    System.err.println("⚠️ 警告: 回傳內容為 HTML，請檢查 Google Apps Script 權限是否設為 'Anyone'");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ 翻譯服務連線例外: " + e.getMessage());
            e.printStackTrace();
        }
        
        return originalText; // 失敗時回傳原文
    }

    // 輔助方法：簡單處理 Unicode 轉義 (如 \u0026 -> &)
    private String decodeUnicode(String input) {
        StringBuilder res = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            char ch = input.charAt(i);
            if (ch == '\\' && i + 1 < input.length() && input.charAt(i + 1) == 'u') {
                try {
                    res.append((char) Integer.parseInt(input.substring(i + 2, i + 6), 16));
                    i += 6;
                    continue;
                } catch (Exception e) { /* ignore */ }
            }
            res.append(ch);
            i++;
        }
        return res.toString();
    }
}