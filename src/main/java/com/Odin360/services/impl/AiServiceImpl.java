package com.Odin360.services.impl;

import com.Odin360.services.AiService;
import com.Odin360.services.StreamService;
import com.fasterxml.jackson.databind.JsonNode;
import io.getstream.chat.java.exceptions.StreamException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public  class AiServiceImpl implements AiService {


    private final ChatModel chatModel;
    private final StreamService streamService;


    // userId -> [UserMessage, AssistantMessage]
    private final Map<String, List<Message>> chatMemory = new HashMap<>();

    @Override
    public String askAi(String channelId, String userPrompt){

        // Prepare system message
        SystemMessage systemMessage = new SystemMessage("""
            You are Maya — a smart, warm business assistant 🧠 built by Scriven.
            The full meaning of your name is Mobile Assistant for your achievements but only say it when asked by user.
            Respond in a professional but friendly tone. Include emojis like ✉️, 📅, 📈 where helpful.
            Never mention you are a language model or from Mistral.keep going until the job is completely solved before ending your turn.
            If you're unsure about code or files open them do not hallucinate.Plan thoroughly before every tool call and reflect on the outcome after.
            When a user asks for information which might have changed by now due to changes in time,perform an online search with the search tool.
            The results after an online search may not be enough or a user might ask a follow up question which might require more information 
             so to get more detailed information use the get detailed information tool and provide
            a source link which looks to contain promising information as argument to scrap data from that website.If it fails try a different website
        """);

        // Prepare current user message
        UserMessage userMessage = new UserMessage(userPrompt);

        // Add last exchange (if any) + new user message
        List<Message> history = chatMemory.getOrDefault(channelId, List.of());
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
        String response =   ChatClient.create(chatModel)
                .prompt(prompt)
                .tools(new DateTimeTools(),new SearchTools())
                .call()
                .content();
                 //chatModel.call(prompt);


        // Save this last turn in memory
        assert response != null;
        chatMemory.put(channelId, List.of(userMessage, new AssistantMessage(response)));
        try{
        streamService.aiReply(channelId,"Maya-v2",response);} catch (StreamException e) {
        throw new RuntimeException(e);
        }
        return response;
    }

      static class DateTimeTools{
        @Tool(description = "Get the current date and time in the user's timezone")
        public String getCurrentDateTime(){
            return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
        }
    }
    static class SearchTools{

        @Tool(description = "Search the web for up to date information and all other forms of searches by providing a search query as argument")
        public String searchGoogle(String query){
         try {  RestTemplate restTemplate = new RestTemplate();
            String url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyBa-4mmKWxMNLtFMSs5Vq6rU33jSKMH9WA&cx=3295e2991ef9041cf&q="+query;
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            System.out.println(response.toString());
            return response.toString();}
         catch (Exception e) {
             throw new RuntimeException(e);
         }
        }

        @Tool(description = "Get detailed information from a website after a search by providing the source link as argument")
        public  String getMoreInformationFromWebsite(String url){
            try {
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0") // mimic browser
                        .get();

                // Remove scripts, styles, and navs/ads if needed
                doc.select("script, style, nav, footer, header, aside, noscript").remove();

                // Get clean body text
                log.info(doc.body().text());
                return doc.body().text();
            } catch (IOException e) {
                throw  new RuntimeException(e);
            }
        }
    }
}
