package com.example.googlesearch.service;

import com.example.googlesearch.model.SearchResult;
import com.example.googlesearch.model.User;
import com.example.googlesearch.model.WebPageNode;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SearchEngine {

    private final GoogleSearchService googleSearchService;
    private final KeywordScorer keywordScorer;
    private final URLRanker urlRanker;
    private final UserWeightManager userWeightManager;
    private final TranslatorBridge translatorBridge;

    @Autowired
    public SearchEngine(GoogleSearchService googleSearchService, 
                        KeywordScorer keywordScorer,
                        URLRanker urlRanker, 
                        UserWeightManager userWeightManager,
                        TranslatorBridge translatorBridge) {
        this.googleSearchService = googleSearchService;
        this.keywordScorer = keywordScorer;
        this.urlRanker = urlRanker;
        this.userWeightManager = userWeightManager;
        this.translatorBridge = translatorBridge;
    }

    public List<SearchResult> searchAndRank(String query, User user, int start, String filter) {
        // 1. 自動翻譯 (日文/中文 -> 英文)
        String effectiveQuery = translatorBridge.translateToEnglish(query);
        
        List<SearchResult> rawResults;
        try {
            // 使用翻譯後的關鍵字搜尋
            rawResults = googleSearchService.search(effectiveQuery, start);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        for (SearchResult result : rawResults) {
            String content = fetchContent(result.getUrl());
            if (content.isEmpty()) {
                content = result.getTitle() + " " + result.getSnippet();
            }

            WebPageNode node = new WebPageNode(result.getTitle(), result.getUrl(), content);

            double keywordScore = keywordScorer.calculateTotalScore(node);
            double baseRank = urlRanker.getBaseRank(node.getUrl());
            double levelWeight = urlRanker.getLevelWeight(node.getLevel());
            double urlScore = baseRank * levelWeight;
            double userWeight = userWeightManager.getUserWeight(user);

            double finalScore = urlScore * userWeight * (keywordScore + 1.0);
            result.setFinalScore(Math.round(finalScore * 100.0) / 100.0);
            
            // 智能標籤 & 預算
            List<String> tags = analyzeTags(node.getContent(), node.getTitle());
            result.setTags(tags);

            String budget = analyzeBudget(node.getContent());
            result.setBudgetLabel(budget);
        }

        // 排序
        Collections.sort(rawResults, Comparator.comparingDouble(SearchResult::getFinalScore).reversed());

        // 篩選邏輯
        if (filter != null && !filter.isEmpty() && !filter.equals("all")) {
            return rawResults.stream()
                .filter(r -> {
                    for (String tag : r.getTags()) {
                        if (tag.contains(filter)) return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        }

        return rawResults;
    }

    // ★★★ Stage 4: 衍生相關關鍵字 (Relative Keywords) ★★★
    // 這就是您報錯日誌中缺少的那個方法！
    public List<String> deriveRelatedKeywords(List<SearchResult> results) {
        if (results == null || results.isEmpty()) return new ArrayList<>();

        // 只分析前 5 名結果
        int limit = Math.min(results.size(), 5);
        List<SearchResult> topResults = results.subList(0, limit);

        List<String> allWords = new ArrayList<>();
        for (SearchResult result : topResults) {
            // 將標題和摘要合併分析
            String text = (result.getTitle() + " " + result.getSnippet()).toLowerCase();
            // 移除標點符號，只留英數字與中文
            String[] words = text.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", " ").split("\\s+");
            
            for (String w : words) {
                // 過濾停用詞與太短的字
                if (w.length() > 2 && !isStopWord(w)) {
                    allWords.add(w);
                }
            }
        }

        // 統計詞頻並取前 6 名
        return allWords.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(6)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private boolean isStopWord(String word) {
        // 簡單的停用詞表
        String stopWords = "the and of to in a is for on with as at be this that it by are from will has have but or not an www com http https guide best top review copenhagen denmark";
        return stopWords.contains(word) || word.matches("\\d+");
    }

    // --- 其他輔助方法 (保持不變) ---

    private List<String> analyzeTags(String content, String title) {
        List<String> tags = new ArrayList<>();
        String text = (title + " " + content).toLowerCase();
        if (text.matches(".*(restaurant|food|menu|delicious|cafe|coffee|dinner|lunch|美食|餐廳|好吃|菜單|美味).*")) tags.add("美食 🍴");
        if (text.matches(".*(hotel|hostel|accommodation|booking|room|bnb|住宿|飯店|民宿|訂房).*")) tags.add("住宿 🏨");
        if (text.matches(".*(museum|park|tour|guide|ticket|sightseeing|attraction|view|景點|博物館|公園|門票|參觀).*")) tags.add("景點 🎡");
        if (text.matches(".*(transport|train|bus|metro|ticket|airport|station|rail|交通|火車|巴士|機場|地鐵|車站).*")) tags.add("交通 🚆");
        if (text.matches(".*(shopping|mall|store|buy|gift|souvenir|購物|伴手禮|必買|商場).*")) tags.add("購物 🛍️");
        return tags;
    }

    private String analyzeBudget(String content) {
        String lowerContent = content.toLowerCase();
        if (lowerContent.matches(".*(dkk|kr\\.?)\\s*[1-9]\\d{3,}.*")) return "$$$";
        else if (lowerContent.matches(".*(dkk|kr\\.?)\\s*[5-9]\\d{2}.*")) return "$$";
        else if (lowerContent.matches(".*(dkk|kr\\.?)\\s*\\d{1,3}.*")) return "$";
        return "";
    }

    public List<String> getExternalTrendingKeywords() {
        return Arrays.asList("哥本哈根旅遊攻略", "丹麥必買伴手禮", "北歐極光行程", "安徒生童話景點", "Copenhagen Card", "丹麥米其林餐廳");
    }

    public Map<String, List<SearchResult>> generateReportContent(Map<String, List<SearchResult>> itinerary) {
        for (Map.Entry<String, List<SearchResult>> entry : itinerary.entrySet()) {
            for (SearchResult item : entry.getValue()) {
                String fullContent = fetchContent(item.getUrl());
                String summary = (fullContent.length() > 300) ? fullContent.substring(0, 300) + "..." : (fullContent.isEmpty() ? "無法讀取詳細內容" : fullContent);
                item.setSnippet(summary);
            }
        }
        return itinerary;
    }

    private String fetchContent(String url) {
        try {
            return Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(3000).get().body().text();
        } catch (Exception e) { return ""; }
    }
}