package com.example.taskmaster.service;

import org.springframework.stereotype.Service;


@Service
public class ValidationService {

    private static final String[] bannedWords = {
        ";", "&", "|", "`", "rm ", "mv ", "cp ", "dd ", ">", "<"
    };

    public String validate(String cmd) {
        String normalized = cmd.toLowerCase().trim();
        for (String w : bannedWords) {
            if (normalized.contains(w)) {
                return w;
            }
        }
        return null;
    }
}