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

    // ★★★ 關鍵修正：這裡必須接收 int start 參數 ★★★
    public List<SearchResult> search(String query, int start) throws IOException {
        System.out.println("Search query: " + query + ", start: " + start);

        // 1. 嘗試使用 Google API
        if (cseEnabled && apiKey != null && !apiKey.isEmpty()) {
            try {
                return searchWithApi(query, start);
            } catch (Exception e) {
                System.err.println("API 呼叫失敗，切換回爬蟲模式: " + e.getMessage());
                // API 失敗會自動往下走，執行爬蟲
            }
        }

        // 2. 備援：使用爬蟲
        // 注意：這裡呼叫 googleQuery.setSearchParameters 也需要 start
        // 如果你的 GoogleQuery.java 還沒改好，這裡也會報錯，請務必確認 GoogleQuery 也更新了
        googleQuery.setSearchParameters(query, start);
        return googleQuery.query();
    }

    private List<SearchResult> searchWithApi(String query, int start) {
        List<SearchResult> results = new ArrayList<>();
        try {
            String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
            // API 的 start 是從 1 開始 (1, 11, 21...)，但如果傳入 0 要轉成 1
            int apiStart = (start < 1) ? 1 : start;
            
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
                            String snippet = (String) item.get("snippet"); // 這裡抓取摘要
                            
                            if (title != null && link != null) {
                                results.add(new SearchResult(title, link, snippet));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Google CSE 連線錯誤", e);
        }
        return results;
    }
}