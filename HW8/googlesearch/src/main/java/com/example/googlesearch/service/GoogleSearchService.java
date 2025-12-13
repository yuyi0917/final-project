package com.example.googlesearch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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

    public HashMap<String, String> search(String query) throws IOException {
        // Prefer official Google Custom Search API when enabled and configured
        if (cseEnabled && apiKey != null && !apiKey.isEmpty() && cx != null && !cx.isEmpty()) {
            HashMap<String, String> results = new HashMap<>();
            try {
                String q = URLEncoder.encode(query, StandardCharsets.UTF_8.name());
                String url = String.format("https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&num=10&q=%s", apiKey, cx, q);
                ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
                Map body = resp.getBody();
                if (body != null) {
                    Object itemsObj = body.get("items");
                    if (itemsObj instanceof List) {
                        List items = (List) itemsObj;
                        for (Object itemObj : items) {
                            if (itemObj instanceof Map) {
                                Map item = (Map) itemObj;
                                Object titleObj = item.get("title");
                                Object linkObj = item.get("link");
                                if (titleObj != null && linkObj != null) {
                                    results.put(titleObj.toString(), linkObj.toString());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new IOException("Error calling Google CSE: " + e.getMessage(), e);
            }
            return results;
        }

        // Fallback: HTML scraping using GoogleQuery
        googleQuery.setSearchKeyword(query);
        return googleQuery.query();
    }
}