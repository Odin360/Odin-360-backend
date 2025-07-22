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
    @GetMapping("/{channelId}/askAi")
    public ResponseEntity<String> askAi(@PathVariable String channelId, @RequestParam String prompt){
        return ResponseEntity.ok(aiService.askAi(channelId,prompt));
    }
    @GetMapping("/{userId}/voiceAi")
    public ResponseEntity<String> voiceAi(@PathVariable String userId, @RequestParam String prompt){
        return ResponseEntity.ok(aiService.voiceAi(userId,prompt));
    }
}
