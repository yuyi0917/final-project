package com.example.googlesearch.model;

import java.time.Instant;
import org.springframework.util.StringUtils;

public class WebPageNode {
    private String url;
    private String title;
    private String content; // 網頁的全部內容
    private int level;      // 網頁在樹結構中的層級 (以URL深度模擬)
    private Instant lastModified;
    
    public WebPageNode(String title, String url, String content) {
        this.title = title;
        this.url = url;
        this.content = content != null ? content : "";
        this.lastModified = Instant.now();
        this.level = calculateLevel(url);
    }

    private int calculateLevel(String url) {
        if (url == null) return 0;
        // 簡單透過 / 的數量來模擬層級，層級越深分數可能受影響
        return StringUtils.countOccurrencesOf(url, "/") - 2; // 扣除 http:// 的部分
    }

    public int countKeyword(String keyword) {
        if (content == null || content.isEmpty() || keyword == null) return 0;
        // 忽略大小寫計算出現次數
        String lowerContent = content.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        return StringUtils.countOccurrencesOf(lowerContent, lowerKeyword);
    }

    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getLevel() { return level; }
    public Instant getLastModified() { return lastModified; }
}