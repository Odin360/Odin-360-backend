package com.Odin360.controllers;

import com.Odin360.services.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiController {
    private final AiService aiService;
    @GetMapping("/{userId}/askAi")
    public ResponseEntity<String> askAi(@PathVariable UUID userId, @RequestParam String prompt){
        return ResponseEntity.ok(aiService.askAi(userId,prompt));
    }
}
