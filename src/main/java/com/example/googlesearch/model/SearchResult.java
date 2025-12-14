package com.example.googlesearch.model;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
    private String title;
    private String url;
    private String snippet;
    private double finalScore;
    
    // --- 新增功能欄位 ---
    private List<String> tags = new ArrayList<>(); // 智能標籤
    private String budgetLabel; // 預算預測 ($, $$, $$$)

    // ★★★ 必須保留此空建構子，否則報告功能會報錯 ★★★
    public SearchResult() {
    }

    public SearchResult(String title, String url, String snippet) {
        this(title, url, snippet, 0.0);
    }

    public SearchResult(String title, String url, String snippet, double finalScore) {
        this.title = title;
        this.url = url;
        this.snippet = snippet;
        this.finalScore = finalScore;
    }

    // --- Getters & Setters ---

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore = finalScore; }

    // 新增欄位的 Getter/Setter
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void addTag(String tag) { this.tags.add(tag); }

    public String getBudgetLabel() { return budgetLabel; }
    public void setBudgetLabel(String budgetLabel) { this.budgetLabel = budgetLabel; }
}