package com.example.googlesearch.service;

import com.example.googlesearch.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserWeightManager {
    
    public double getUserWeight(User user) {
        // 根據使用者權限等級回傳權重
        if ("VIP".equalsIgnoreCase(user.getAccessLevel())) {
            return 1.5;
        }
        return 1.0;
    }
}