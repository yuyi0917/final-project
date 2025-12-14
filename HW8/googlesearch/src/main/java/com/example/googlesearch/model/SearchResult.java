package com.example.googlesearch.model;

public class SearchResult {
    private String title;
    private String url;
    private String snippet;
    private double finalScore;

    // ★★★ 必須加入這個無參數建構子，否則 @RequestBody 會報 500 錯誤 ★★★
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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore = finalScore; }
}