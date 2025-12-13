package com.example.googlesearch.controller;

import com.example.googlesearch.service.GoogleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

@Controller
public class SearchController {

    @Autowired
    private GoogleSearchService searchService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam String query, Model model) {
        try {
            HashMap<String, String> results = searchService.search(query);
            model.addAttribute("results", results);
            model.addAttribute("query", query);
            return "results";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "index";
        }
    }
}