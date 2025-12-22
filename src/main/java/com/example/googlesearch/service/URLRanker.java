package com.example.googlesearch.service;

import org.springframework.stereotype.Component;

@Component
public class URLRanker {
    
    /**
     * 根據 URL 的結構特徵給予基礎評分
     * 治本策略：從網址結構直接辨識電商與非旅遊頁面
     */
    public double getBaseRank(String url) {
        if (url == null) return 0.0;
        String lowerUrl = url.toLowerCase();
        
        // --- 1. 【強力過濾】電商與功能性頁面特徵 (直接降為極低分 0.1) ---
        // 這些是「治本」的關鍵：無論關鍵字多吻合，只要網址長這樣，通常就是商店
        if (lowerUrl.contains("/product/") || 
            lowerUrl.contains("/shop/") || 
            lowerUrl.contains("/store/") ||
            lowerUrl.contains("/cart") || 
            lowerUrl.contains("/checkout") || 
            lowerUrl.contains("/basket") ||
            lowerUrl.contains("/login") || 
            lowerUrl.contains("/register") ||
            lowerUrl.contains("/account") ||
            lowerUrl.contains("ebay.") || 
            lowerUrl.contains("amazon.")) {
            return 0.1; // 幾乎殺死該結果，確保它不會排在前面
        }

        double score = 1.0;

        // --- 2. 【網域權威性】旅遊導向加分 ---
        if (lowerUrl.contains(".dk")) score += 0.2; // 丹麥在地網域
        if (lowerUrl.contains("visitdenmark")) score += 1.5; // 官方旅遊局 (最高權威)
        if (lowerUrl.contains("tripadvisor")) score += 0.8;
        if (lowerUrl.contains("wikipedia")) score += 0.5;
        if (lowerUrl.contains("lonelyplanet")) score += 0.8;
        if (lowerUrl.contains("natgeo")) score += 0.6; // 國家地理

        // --- 3. 【路徑語意】包含旅遊相關路徑 ---
        // 網址包含這些詞，通常代表是文章或介紹
        if (lowerUrl.contains("/guide") || 
            lowerUrl.contains("/attraction") || 
            lowerUrl.contains("/travel") || 
            lowerUrl.contains("/history") ||
            lowerUrl.contains("/culture") ||
            lowerUrl.contains("/sightseeing")) {
            score += 0.5;
        }

        return score;
    }

    // 計算層級權重 (層級越深，權重稍微降低)
    public double getLevelWeight(int level) {
        return Math.max(0.5, 1.0 - (level * 0.05));
    }
}