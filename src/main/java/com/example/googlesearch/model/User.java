package com.example.googlesearch.model;

import java.util.HashMap;
import java.util.Map;

public class User {
    private int userId;
    private String accessLevel;
    private Map<String, Double> topicPreference;

    public User(int userId, String accessLevel) {
        this.userId = userId;
        this.accessLevel = accessLevel;
        this.topicPreference = new HashMap<>();
        // 預設偏好，可擴充
        this.topicPreference.put("丹麥", 1.2);
        this.topicPreference.put("旅遊", 1.0);
    }

    public int getUserId() { return userId; }
    public String getAccessLevel() { return accessLevel; }
    
    public double getTopicPreference(String topic) {
        return topicPreference.getOrDefault(topic, 1.0);
    }
}