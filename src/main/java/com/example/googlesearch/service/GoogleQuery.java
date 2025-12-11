package com.example.googlesearch.service;

import com.example.googlesearch.model.SearchResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleQuery {
    private String url;

    public GoogleQuery() {
        // Default constructor
    }

    // 配合 GoogleSearchService 呼叫的方法簽章
    public void setSearchParameters(String searchKeyword, int start) {
        try {
            String encodeKeyword = URLEncoder.encode(searchKeyword, StandardCharsets.UTF_8);
            
            // Google 網頁版的 start 參數是從 0 開始 (0=第1頁, 10=第2頁)
            // 而通常 UI 或 API 傳入的是 1 開始 (1=第1頁, 11=第2頁)
            // 這裡做個防呆與轉換
            int webStart = 0;
            if (start > 1) {
                webStart = start - 1;
            }
            
            this.url = "https://www.google.com/search?q=" + encodeKeyword + "&oe=utf8&num=10&start=" + webStart;
        } catch (Exception e) {
            System.out.println("Encoding error: " + e.getMessage());
        }
    }

    public List<SearchResult> query() throws IOException {
        List<SearchResult> results = new ArrayList<>();
        
        if (this.url == null) {
            return results;
        }

        // 使用 Jsoup 連線 (設定 User-Agent 避免 403 Forbidden 或取得舊版頁面)
        Document doc = Jsoup.connect(this.url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(10000)
                .get();

        // 選擇電腦版搜尋結果區塊 div.g
        Elements gElements = doc.select("div.g");

        for (Element g : gElements) {
            try {
                Element h3 = g.select("h3").first();
                Element a = g.select("a").first();
                // 抓取摘要 (div.VwiC3b 是常見的摘要 class)
                Element snippetDiv = g.select("div.VwiC3b").first();

                // 若主要 class 抓不到，嘗試抓取備用結構
                if (snippetDiv == null) {
                    snippetDiv = g.select("div[style*='-webkit-line-clamp']").first();
                }

                if (h3 != null && a != null) {
                    String title = h3.text();
                    String link = a.attr("href");
                    String snippet = (snippetDiv != null) ? snippetDiv.text() : "";

                    if (!title.isEmpty() && !link.isEmpty()) {
                        // 清理 Google 可能回傳的跳轉連結
                        if (link.startsWith("/url?q=")) {
                            link = link.replace("/url?q=", "").split("&")[0];
                        }
                        
                        // 注意：這裡假設您的 SearchResult 有 (String title, String url, String snippet) 的建構子
                        // 如果您的 SearchResult 只有 (title, url)，請刪除 snippet 參數
                        results.add(new SearchResult(title, link, snippet));
                    }
                }
            } catch (Exception e) {
                // 忽略單筆解析失敗，繼續處理下一筆
                // e.printStackTrace();
            }
        }

        return results;
    }
}