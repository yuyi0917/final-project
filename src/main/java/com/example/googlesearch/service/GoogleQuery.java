package com.example.googlesearch.service;

import com.example.googlesearch.model.SearchResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleQuery {
    private String url;

    public GoogleQuery() {
    }

    // 設定搜尋參數，將頁碼轉換為 Google 的 start 參數
    public void setSearchParameters(String searchKeyword, int page) {
        try {
            String encodeKeyword = URLEncoder.encode(searchKeyword, StandardCharsets.UTF_8);
            // Google 的 start 參數：第1頁=0, 第2頁=10...
            int start = (page - 1) * 10;
            if (start < 0) start = 0;
            this.url = "https://www.google.com/search?q=" + encodeKeyword + "&oe=utf8&num=10&start=" + start;
        } catch (Exception e) {
            System.out.println("Encoding error: " + e.getMessage());
        }
    }

    public List<SearchResult> query() throws IOException {
        List<SearchResult> results = new ArrayList<>();
        if (this.url == null) return results;

        System.out.println("爬蟲目標 URL: " + this.url);

        // 1. 連線並獲取文件 (偽裝成瀏覽器)
        Document doc = Jsoup.connect(this.url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                // ★★★ 關鍵修正：加入 Cookie，略過 Google 同意頁面 ★★★
                .header("Cookie", "CONSENT=YES+;")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                // 強制使用英文介面，結構比較穩定，避免不同語言的 HTML 差異
                .header("Accept-Language", "en-US,en;q=0.9")
                .timeout(10000)
                .get();

        System.out.println("爬取到的網頁標題: " + doc.title());

        // 2. 抓取結果區塊
        // 優先嘗試標準結構 div.g
        Elements resultElements = doc.select("div.g");
        
        // 如果抓不到，嘗試更通用的結構 (包含 h3 標題的 div 區塊)
        if (resultElements.isEmpty()) {
            System.out.println("標準 div.g 抓不到，嘗試通用結構分析...");
            resultElements = doc.select("div:has(h3)");
        }

        System.out.println("找到的潛在區塊數量: " + resultElements.size());

        // ★★★ 除錯機制：如果抓不到結果，存檔 HTML 供檢查 ★★★
        if (resultElements.isEmpty()) {
            System.err.println("警報：抓不到任何結果！正在將 HTML 存為 google_debug.html 以便檢查...");
            try (FileWriter writer = new FileWriter(new File("google_debug.html"))) {
                writer.write(doc.html());
                System.out.println("HTML 已存檔，請在專案根目錄打開 google_debug.html 查看內容。");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

        // 3. 解析內容
        for (Element el : resultElements) {
            try {
                // 找標題 (h3)
                Element h3 = el.select("h3").first();
                // 找連結 (a)
                Element a = el.select("a").first();
                
                // 標題與連結必須存在才算有效結果
                if (h3 == null || a == null) continue;

                String title = h3.text();
                String link = a.attr("href");
                
                // 找摘要 (嘗試多種常見的摘要 class)
                String snippet = "";
                Element snippetDiv = el.select("div.VwiC3b").first(); 
                if (snippetDiv == null) snippetDiv = el.select("div[style*='-webkit-line-clamp']").first();
                if (snippetDiv == null) snippetDiv = el.select("div.ITZIwc").first();
                
                if (snippetDiv != null) snippet = snippetDiv.text();

                // 連結清理
                if (link.startsWith("/url?q=")) {
                    link = link.split("&")[0].replace("/url?q=", "");
                }
                
                // 簡單過濾：只保留 HTTP 開頭的有效網址
                if (title.length() > 0 && link.startsWith("http")) {
                    results.add(new SearchResult(title, link, snippet));
                }

            } catch (Exception e) {
                // 忽略解析錯誤的單筆，繼續下一筆
            }
        }

        // 4. 去除重複結果 (因為通用選擇器可能會重複抓到同一區塊)
        List<SearchResult> uniqueResults = new ArrayList<>();
        List<String> seenUrls = new ArrayList<>();
        for (SearchResult r : results) {
            if (!seenUrls.contains(r.getUrl())) {
                uniqueResults.add(r);
                seenUrls.add(r.getUrl());
            }
        }
        
        System.out.println("有效結果數量: " + uniqueResults.size());
        return uniqueResults;
    }
}