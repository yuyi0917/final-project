package com.example.googlesearch.controller;

import com.example.googlesearch.model.SearchResult;
import com.example.googlesearch.model.User;
import com.example.googlesearch.service.SearchEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Controller
public class SearchController {

    @Autowired
    private SearchEngine searchEngine;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("trending", searchEngine.getExternalTrendingKeywords());
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam String query, 
                         @RequestParam(defaultValue = "1") int start,
                         @RequestParam(required = false) String filter,
                         Model model) {
        try {
            User currentUser = new User(1, "Normal");
            
            // 執行搜尋與排名
            List<SearchResult> results = searchEngine.searchAndRank(query, currentUser, start, filter);
            
            // ★ Stage 4: 取得衍生相關關鍵字 (這裡呼叫 Engine 的新方法)
            List<String> relatedKeywords = searchEngine.deriveRelatedKeywords(results);
            
            model.addAttribute("results", results);
            model.addAttribute("query", query);
            model.addAttribute("currentStart", start);
            model.addAttribute("currentFilter", filter);
            
            // 傳給前端
            model.addAttribute("relatedKeywords", relatedKeywords); 
            model.addAttribute("trending", searchEngine.getExternalTrendingKeywords());
            
            return "results";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "index";
        }
    }

    @PostMapping("/report")
    public String generateReport(@RequestBody Map<String, List<SearchResult>> itinerary, Model model) {
        try {
            Map<String, List<SearchResult>> enrichedItinerary = searchEngine.generateReportContent(itinerary);
            model.addAttribute("itinerary", enrichedItinerary);
            return "report";
        } catch (Exception e) {
            e.printStackTrace();
            return "index";
        }
    }

    @GetMapping("/suggest")
    @ResponseBody
    public String suggest(@RequestParam String q) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://suggestqueries.google.com/complete/search?client=firefox&q=" + q;
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            return "[]";
        }
    }
}