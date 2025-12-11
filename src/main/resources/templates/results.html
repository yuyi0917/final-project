package com.example.googlesearch.controller;

import com.example.googlesearch.model.SearchResult;
import com.example.googlesearch.model.User;
import com.example.googlesearch.service.SearchEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private SearchEngine searchEngine; // 改用 SearchEngine

    private final List<String> searchHistory = new ArrayList<>();

    @GetMapping("/")
    public String index(Model model) {
        int size = searchHistory.size();
        List<String> recent = searchHistory.subList(Math.max(0, size - 5), size);
        model.addAttribute("history", recent);
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam String query, 
                         @RequestParam(defaultValue = "1") int start, 
                         Model model) {
        try {
            if (!query.trim().isEmpty()) {
                searchHistory.remove(query);
                searchHistory.add(query);
            }

            // 模擬一個使用者 (可未來擴充登入功能)
            User currentUser = new User(1, "Normal");

            // 使用 SearchEngine 進行搜尋與排序
            List<SearchResult> results = searchEngine.searchAndRank(query, currentUser, start);
            
            model.addAttribute("results", results);
            model.addAttribute("query", query);
            model.addAttribute("currentStart", start);
            
            return "results";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
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