package com.Odin360.services;

import io.getstream.chat.java.exceptions.StreamException;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public interface AiService {
    String askAi(String channelId, String userPrompt);
    String voiceAi(String userId, String prompt);
}
