package com.example.googlesearch.service;

import com.example.googlesearch.model.SearchResult;
import com.example.googlesearch.model.User;
import com.example.googlesearch.model.WebPageNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class SearchEngine {

    private final GoogleSearchService googleSearchService; // 用來抓初始名單
    private final KeywordScorer keywordScorer;
    private final URLRanker urlRanker;
    private final UserWeightManager userWeightManager;

    @Autowired
    public SearchEngine(GoogleSearchService googleSearchService, KeywordScorer keywordScorer, 
                        URLRanker urlRanker, UserWeightManager userWeightManager) {
        this.googleSearchService = googleSearchService;
        this.keywordScorer = keywordScorer;
        this.urlRanker = urlRanker;
        this.userWeightManager = userWeightManager;
    }

    public List<SearchResult> searchAndRank(String query, User user, int start) {
        List<SearchResult> rawResults;
        try {
            // 1. 取得 Google 原始結果 (Top 10)
            rawResults = googleSearchService.search(query, start);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        // 2. 針對每個結果建立 WebPageNode 並爬取內容進行評分
        for (SearchResult result : rawResults) {
            String content = fetchContent(result.getUrl());
            // 如果爬蟲失敗，就用 Snippet 當作內容的替代品，避免分數為 0
            if (content.isEmpty()) {
                content = result.getTitle() + " " + result.getSnippet();
            }

            WebPageNode node = new WebPageNode(result.getTitle(), result.getUrl(), content);

            // 3. 計算各項分數
            // Keyword Score
            double keywordScore = keywordScorer.calculateTotalScore(node);
            
            // URL Score
            double baseRank = urlRanker.getBaseRank(node.getUrl());
            double levelWeight = urlRanker.getLevelWeight(node.getLevel());
            double urlScore = baseRank * levelWeight;

            // User Weight
            double userWeight = userWeightManager.getUserWeight(user);

            // 4. 最終公式 (參考 PDF: W_user * R_base * W_level * KeywordScore)
            // 這裡做一點調整以避免分數過大或過小: Base * User * (KeywordScore + 1)
            double finalScore = urlScore * userWeight * (keywordScore + 1.0);
            
            // 將分數寫回 Result
            result.setFinalScore(Math.round(finalScore * 100.0) / 100.0); // 取小數點兩位
        }

        // 5. 根據分數排序 (由高到低)
        Collections.sort(rawResults, Comparator.comparingDouble(SearchResult::getFinalScore).reversed());

        return rawResults;
    }

    // 簡單的爬蟲方法
    private String fetchContent(String url) {
        try {
            // 設定 Timeout 避免卡太久，每個網頁限時 3 秒
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(3000) 
                    .get();
            return doc.body().text(); // 只取文字內容
        } catch (Exception e) {
            // System.err.println("Failed to fetch: " + url);
            return "";
        }
    }
}