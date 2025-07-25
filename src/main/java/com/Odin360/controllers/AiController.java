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
    @GetMapping("/{channelId}/{userId}/{teamId}/askAi")
    public ResponseEntity<String> askAi(@PathVariable String channelId,@PathVariable UUID userId, @RequestParam String prompt,@PathVariable UUID teamId){
        return ResponseEntity.ok(aiService.askAi(channelId,prompt,teamId,userId));
    }
    @GetMapping("/{userId}/{teamId}/voiceAi")
    public ResponseEntity<String> voiceAi(@PathVariable UUID userId, @RequestParam String prompt,@PathVariable UUID teamId){
        return ResponseEntity.ok(aiService.voiceAi(userId,prompt,teamId));
    }
    @GetMapping("/{channelId}/{userId}/askAi")
    public ResponseEntity<String> askAiNoTeam(@PathVariable String channelId,@PathVariable UUID userId, @RequestParam String prompt){
        return ResponseEntity.ok(aiService.askAiNoTeam(channelId,userId,prompt));
    }

    @GetMapping("/{userId}/voiceAi")
    public ResponseEntity<String> voiceAiNoTeam(@PathVariable UUID userId, @RequestParam String prompt){
        return ResponseEntity.ok(aiService.voiceAiNoTeam(userId,prompt));  }

    @GetMapping("/searchAi")
        public ResponseEntity<String> searchAi(@RequestParam String prompt){
            return ResponseEntity.ok(aiService.searchWithMaya(prompt));

    }
}
