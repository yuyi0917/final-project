package com.example.googlesearch.service;

import com.example.googlesearch.model.WebPageNode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KeywordScorer {
    private Map<String, Double> keywordWeights;

    public KeywordScorer() {
        keywordWeights = new HashMap<>();
        initializeKeywords();
    }

    private void initializeKeywords() {
        // --- 加分項 (權重 1.0 ~ 0.7) ---
        // 1. 主要城市
        addKeywords(1.0, "哥本哈根", "København", "奧胡斯", "Århus", "歐登塞", "Odense", "奧爾堡", "Aalborg", "腓特烈斯貝", "Frederiksberg");
        
        // 2. 觀光景點
        addKeywords(1.0, 
            "新港", "Nyhavn", "趣伏裡公園", "Tivoli Gardens", "小美人魚雕像", "The Little Mermaid", 
            "阿馬林堡宮", "Amalienborg Palace", "圓塔", "Rundetårn", "奧爾胡斯藝術博物館", "AROS",
            "老城露天博物館", "Den Gamle By", "奧爾胡斯大教堂", "Aarhus Domkirke", "莫斯加史前歷史博物館", "Moesgaard Museum",
            "奧胡斯市政廳", "Aarhus Rådhus", "安徒生博物館", "H.C. Andersen Museum", "安徒生故居",
            "歐登塞扇形火車博物館", "Danmarks Jernbanemuseum", "聖克努特大教堂", "Sankt Knuds Kirke", "菲英村", "Funen Village",
            "奧爾堡現代藝術博物館", "奧爾堡城堡", "奧爾堡動物園", "奧爾堡歷史博物館", "耶格斯布洛姆修道院",
            "腓特烈斯貝花園", "哥本哈根動物園", "腓特烈斯貝宮", "穿牆市場", "市集廣場"
        );

        // 3. 文化與美食 (權重 0.7)
        addKeywords(0.7, 
            "Hygge", "單車文化", "Cykelkultur", "新北歐美食", "New Nordic Cuisine",
            "微笑之城", "Smilets By", "彩虹全景", "Rainbow Panorama", "童話之城", "Fairy Tale City",
            "開放式三明治", "Smørrebrød", "丹麥肉丸", "Frikadeller", "烤豬肉", "Flæskesteg", "丹麥酥餅", "Wienerbrød"
        );

        // --- 扣分項 (權重 -0.3) ---
        // 外國撞名地點
        addKeywords(-0.3, 
            "New York", "USA", "South Carolina", "KwaZulu-Natal", "South Africa", 
            "Wisconsin", "Alberta", "Canada", "Virginia", "Texas", "Cape Province"
        );
    }

    private void addKeywords(double weight, String... keywords) {
        for (String k : keywords) {
            keywordWeights.put(k, weight);
        }
    }

    public double getKeywordWeight(String keyword) {
        return keywordWeights.getOrDefault(keyword, 0.0);
    }

    // Σ(C_j * W_j)
    public double calculateTotalScore(WebPageNode node) {
        double totalScore = 0.0;
        
        // 1. Title Match (*10)
        for (Map.Entry<String, Double> entry : keywordWeights.entrySet()) {
            String k = entry.getKey();
            double w = entry.getValue();
            
            // 標題權重加成 10 倍 (根據 PDF 公式)
            if (node.getTitle().toLowerCase().contains(k.toLowerCase())) {
                totalScore += 10 * w; 
            }
            
            // 2. Content Match (*1)
            int count = node.countKeyword(k);
            if (count > 0) {
                totalScore += count * w;
            }
        }
        
        // 3. Tag Match (*5) - 這裡簡化為 snippet 中出現的關鍵字視為 Tag
        // 因為一般爬蟲抓不到 Meta tags，我們用 snippet 當作 Tag 的近似值
        // 這裡需要從 SearchResult 傳遞 snippet 資訊，但 WebPageNode 目前以 Content 為主
        // 我們假設 Content 的前 200 字包含了 Tag 資訊
        
        return totalScore;
    }
}