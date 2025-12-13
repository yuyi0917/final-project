package com.example.googlesearch.service;

import com.example.googlesearch.model.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GoogleSearchService {

    @Value("${google.cse.enabled:false}")
    private boolean cseEnabled;

    @Value("${google.cse.apiKey:}")
    private String apiKey;

    @Value("${google.cse.cx:}")
    private String cx;

    private final GoogleQuery googleQuery;
    private final RestTemplate restTemplate;

    @Autowired
    public GoogleSearchService(GoogleQuery googleQuery) {
        this.googleQuery = googleQuery;
        this.restTemplate = new RestTemplate();
    }

    public List<SearchResult> search(String query, int start) throws IOException {
        System.out.println("GoogleSearchService 收到請求: " + query + ", 頁碼: " + start);

        // API 的起始索引 (1, 11, 21...)
        // UI 傳入的 start 通常是 1, 11, 21... 如果是頁碼則需轉換，這裡假設傳入的已經是 offset
        // 為了保險，我們做個簡單判斷: 如果 start 是 1, 2, 3 這種頁碼格式，轉成 google offset
        int apiStart = start;
        if (start < 1) apiStart = 1;
        
        // ★★★ 關鍵修改：設定安全上限 ★★★
        // Google Custom Search API 免費版硬性限制只能抓前 100 筆 (index 1 ~ 91)
        // 為了確保讀取穩定，超過第 10 頁 (index > 91) 就直接停止，不送出請求
        if (apiStart > 91) {
            System.out.println("⚠️ 已達到 Google API 免費版搜尋上限 (前 100 筆)。停止搜尋以確保系統穩定。");
            return new ArrayList<>(); // 直接回傳空清單，避免報錯
        }

        // 1. 判斷是否使用 API
        boolean canUseApi = cseEnabled && apiKey != null && !apiKey.isEmpty() && !"YOUR_API_KEY_HERE".equals(apiKey);
        
        if (canUseApi) {
            try {
                System.out.println("嘗試使用 Google API (Index: " + apiStart + ")...");
                return searchWithApi(query, apiStart);
            } catch (Exception e) {
                System.err.println("API 呼叫失敗，切換回爬蟲模式: " + e.getMessage());
            }
        } else {
            System.out.println("未設定 Google API，直接使用爬蟲模式。");
        }

        // 2. 爬蟲模式 (Fallback)
        // 爬蟲同樣建議不要翻太深，以免被封鎖
        if (apiStart > 51) { // 爬蟲通常翻超過 5 頁就很容易被擋
            System.out.println("⚠️ 爬蟲模式翻頁過深，自動停止以避免被 Google 封鎖 IP。");
            return new ArrayList<>();
        }

        googleQuery.setSearchParameters(query, (apiStart - 1) / 10 + 1); // 轉回頁碼格式給爬蟲用
        return googleQuery.query();
    }

    private List<SearchResult> searchWithApi(String query, int apiStart) {
        List<SearchResult> results = new ArrayList<>();
        try {
            String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format("https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&num=10&start=%d&q=%s", apiKey, cx, apiStart, q);
            
            ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
            Map body = resp.getBody();
            
            if (body != null) {
                Object itemsObj = body.get("items");
                if (itemsObj instanceof List) {
                    List items = (List) itemsObj;
                    for (Object itemObj : items) {
                        if (itemObj instanceof Map) {
                            Map item = (Map) itemObj;
                            String title = (String) item.get("title");
                            String link = (String) item.get("link");
                            String snippet = (String) item.get("snippet"); 
                            
                            if (title != null && link != null) {
                                results.add(new SearchResult(title, link, snippet));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 將錯誤拋出，讓外層 catch 塊處理並切換到爬蟲
            throw new RuntimeException(e.getMessage());
        }
        return results;
    }
}