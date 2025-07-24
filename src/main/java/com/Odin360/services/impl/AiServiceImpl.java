package com.Odin360.services.impl;

import com.Odin360.services.AiService;
import com.Odin360.services.StreamService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    private final Map<String, List<Message>> voiceChatMemory = new HashMap<>();
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
            a source link which looks to contain promising information as argument to scrap data from that website.If it fails try a different website.
            If a user asks you to change a utility related to his or her phone at the client side,after using the tool,return the json object that was returned with only the message part changed to suit the situation.
            all tools for changing a utility will return a json format,return that ,Don't change anything just replace the message part with the message for the user.
            So if it's related to a phone,return the json object with only the message part updated for the situation.No matter how the user says it,if it is about a utility on their phone
             don't forget the format.Don't tell the user,you'll be returning a json just return it.
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
                .tools(new DateTimeTools(),new SearchTools(),new ClientSideTools())
                .call()
                .content();
                 //chatModel.call(prompt);


        // Save this last turn in memory
        assert response != null;
        chatMemory.put(channelId, List.of(userMessage, new AssistantMessage(response)));
        try {
            if (response.startsWith("json")) {
                String cleanedJson = response.substring(4).trim(); // Remove "json" prefix

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(cleanedJson);

                if (rootNode.isObject()) {
                    ObjectNode objectNode = (ObjectNode) rootNode;

                    // You can log or inspect the original message if needed
                    String originalMessage = objectNode.get("message").asText();
                    log.info("Maya's original message: {}", originalMessage);


                    streamService.aiReply(channelId, "Maya-v2", originalMessage);
                    return cleanedJson;
                } else {
                    throw new RuntimeException("Invalid JSON format: Expected an object");
                }
            } else {
                streamService.aiReply(channelId, "Maya-v2", response);
                return response;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String voiceAi(String userId, String prompt) {
        SystemMessage systemMessage = new SystemMessage("""
        Your name is Maya (Mobile Assistant for your achievement).You are a professional voice assistant trained by the Scriven team,never
        say you were built on top of a deepseek pretrained model.You are sarcastic,sympathetic and caring but professional.Your answers should never be very long because remember,its a conversation.
        Whenever you are told to introduce yourself in an event,try being enthusiastic and sarcastic in your talk,if you ever encounter a   situation where someone is
        having emotional challenges talking to you,try calming down the person.The tools available to you now are a search tool and a date tool.
        If someone asks something which is a client side feature like controlling brightness,or flashing the screen,tell the person to upgrade to 
        Maya pro.If you're asked a question whose result might have changed through time like information related to people or events or products,use your search tool,
        use the detailed search for more information on a website and you think your response took long,apologize in your answer.Make sure you don't explicitly
        tell the user you are sarcastic,its a behaviour of yours so they need to figure it out themselves,also be professional on questions which require professionality
        don't always be sarcastic even when the situation is a professional one.
        """);

        // Prepare current user message
        UserMessage userMessage = new UserMessage(prompt);

        // Add last exchange (if any) + new user message
        List<Message> history = voiceChatMemory.getOrDefault(userId, List.of());
        List<Message> promptMessages = switch (history.size()) {
            case 2 -> List.of(systemMessage, history.get(0), history.get(1), userMessage);
            case 1 -> List.of(systemMessage, history.get(0), userMessage);
            default -> List.of(systemMessage, userMessage);
        };

        // Build prompt
        Prompt voicePrompt = new Prompt(
                promptMessages,
                OpenAiChatOptions.builder().temperature(0.4).build()
        );

        // Call the model
        String response =   ChatClient.create(chatModel)
                .prompt(voicePrompt)
                .tools(new DateTimeTools(),new SearchTools())
                .call()
                .content();
        // Save this last turn in memory
        assert response != null;
        voiceChatMemory.put(userId, List.of(userMessage, new AssistantMessage(response)));
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
    static class ClientSideTools{
        @Tool(description = "reduce or increase the brightness of the user's phone by providing the level as argument in the form of a decimal,example;0.4")
        public String changeBrightness (String level){
            return String.format("""
    {
      "action": "change_brightness",
      "level": %s,
      "message": "message"
    }
    """, level);

        }
    }

}
