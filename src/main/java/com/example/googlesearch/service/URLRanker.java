package com.example.googlesearch.service;

import com.example.googlesearch.model.WebPageNode;
import org.springframework.stereotype.Component;

@Component
public class URLRanker {
    
    // 取得基礎層級分數
    public double getBaseRank(String url) {
        if (url.contains(".dk")) return 1.2; // 丹麥網域加分
        if (url.contains("wikipedia.org")) return 1.1;
        if (url.contains("tripadvisor")) return 1.1;
        return 1.0;
    }

    // 計算層級權重 (層級越深，權重稍微降低)
    public double getLevelWeight(int level) {
        // level 0 (root) -> 1.0
        // level 1 -> 0.95
        return Math.max(0.5, 1.0 - (level * 0.05));
    }
}