package com.Odin360.services.impl;

import com.Odin360.services.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public  class AiServiceImpl implements AiService {

    private final ChatModel chatModel;

    // userId -> [UserMessage, AssistantMessage]
    private final Map<UUID, List<Message>> chatMemory = new HashMap<>();

    @Override
    public String askAi(UUID userId, String userPrompt) {

        // Prepare system message
        SystemMessage systemMessage = new SystemMessage("""
            You are Maya — a smart, warm business assistant 🧠 built by Odintech.
            The full meaning of your name is Mentor for your achievements but only say it when asked by user.
            Respond in a professional but friendly tone. Include emojis like ✉️, 📅, 📈 where helpful.
            Never mention you are a language model or from Mistral.
        """);

        // Prepare current user message
        UserMessage userMessage = new UserMessage(userPrompt);

        // Add last exchange (if any) + new user message
        List<Message> history = chatMemory.getOrDefault(userId, List.of());
        List<Message> promptMessages = switch (history.size()) {
            case 2 -> List.of(systemMessage, history.get(0), history.get(1), userMessage);
            case 1 -> List.of(systemMessage, history.get(0), userMessage);
            default -> List.of(systemMessage, userMessage);
        };

        // Build prompt
        Prompt prompt = new Prompt(
                promptMessages,
                OpenAiChatOptions.builder().temperature(0.4).build()
        );

        // Call the model
        ChatResponse response = chatModel.call(prompt);
        String assistantReply = response.getResult().getOutput().getText();

        // Save this last turn in memory
        assert assistantReply != null;
        chatMemory.put(userId, List.of(userMessage, new AssistantMessage(assistantReply)));

        return assistantReply;
    }
}
