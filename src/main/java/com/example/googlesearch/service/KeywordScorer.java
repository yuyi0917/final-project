package com.example.googlesearch.service;

import com.example.googlesearch.model.WebPageNode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Component
public class KeywordScorer {
    // 分開儲存兩類關鍵字
    private Map<String, Double> tourismKeywords;
    private Map<String, Double> commercialKeywords; 

    public KeywordScorer() {
        tourismKeywords = new HashMap<>();
        commercialKeywords = new HashMap<>();
        initializeKeywords();
    }

    private void initializeKeywords() {
        // --- A. 旅遊資訊關鍵字 (正面特徵) ---
        // 這些詞越多，代表越像是一篇旅遊攻略或介紹
        addTourism(1.5, "Billund", "Legoland", "LEGO House", "Tivoli", "Nyhavn");
        addTourism(1.2, "Copenhagen", "København", "Aarhus", "Odense", "Denmark");
        addTourism(1.0, "Attraction", "Museum", "Castle", "Palace", "Cathedral", "Park", "Beach");
        addTourism(0.8, "Itinerary", "Guide", "History", "Culture", "Architecture", "Opening Hours", "Tickets", "Admission");
        addTourism(0.6, "Restaurant", "Cafe", "Food", "Hygge", "Transport", "Train", "Metro", "View");

        // --- B. 商業/交易意圖關鍵字 (負面特徵) ---
        // 這些詞彙代表網頁意圖是「交易」而非「資訊」
        // 這是治本的關鍵：不論賣什麼，商店網站一定會有這些功能詞
        addCommercial(2.0, "Add to Cart", "Shopping Cart", "Checkout", "Buy Now", "Basket");
        addCommercial(1.5, "Free Shipping", "Delivery", "In Stock", "Out of Stock", "Discount Code", "Promo");
        addCommercial(1.2, "Login", "Register", "Sign Up", "My Account", "Wishlist", "Returns Policy");
        addCommercial(1.0, "Price:", "DKK", "USD", "EUR"); // 單純出現價格符號需警惕，但可能出現在門票資訊，故權重較低
    }

    private void addTourism(double weight, String... keywords) {
        for (String k : keywords) tourismKeywords.put(k, weight);
    }
    
    private void addCommercial(double weight, String... keywords) {
        for (String k : keywords) commercialKeywords.put(k, weight);
    }

    public double calculateTotalScore(WebPageNode node) {
        String contentLower = node.getContent() != null ? node.getContent().toLowerCase() : "";
        String titleLower = node.getTitle() != null ? node.getTitle().toLowerCase() : "";
        
        // 1. 計算「旅遊資訊分數」 (Information Score)
        double tourismScore = calculateScore(tourismKeywords, contentLower, titleLower);
        
        // 2. 計算「商業意圖分數」 (Commercial Score)
        double commercialScore = calculateScore(commercialKeywords, contentLower, titleLower);

        // --- 3. 【治本邏輯】意圖對抗 (Intent Adversarial Check) ---
        
        // 情況 A：這是一個純商店頁面
        // 特徵：商業關鍵字得分很高，且甚至高過了旅遊資訊得分
        // 例如：Lego Store 頁面，會有大量的 "Cart", "Price", "Buy"，但 "History", "Guide" 很少
        if (commercialScore > 5.0 && commercialScore > tourismScore) {
            // 懲罰：將分數降為原本的 10%，直接讓它沉底
            return tourismScore * 0.1; 
        }
        
        // 情況 B：這是一個包含票務資訊的旅遊頁面 (例如博物館官網)
        // 特徵：有 "Price", "Buy Tickets" (產生商業分)，但 "History", "Exhibition" 更多 (旅遊分更高)
        // 處理：只扣除部分商業分數，保留大部分旅遊分數
        if (commercialScore > 0) {
            return Math.max(0.0, tourismScore - (commercialScore * 0.3));
        }

        // 情況 C：純資訊頁面 (無商業干擾)
        return tourismScore;
    }

    // 通用分數計算邏輯
    private double calculateScore(Map<String, Double> keywords, String content, String title) {
        double score = 0.0;
        for (Map.Entry<String, Double> entry : keywords.entrySet()) {
            String k = entry.getKey().toLowerCase();
            double w = entry.getValue();
            
            // Title Match (3倍權重)
            if (title.contains(k)) score += 3.0 * w;
            
            // Content Match (使用 Math.sqrt 防止惡意堆砌)
            int count = StringUtils.countOccurrencesOf(content, k);
            if (count > 0) {
                score += Math.sqrt(count) * w;
            }
        }
        return score;
    }
}