package com.Odin360.services.impl;

import com.Odin360.Domains.entities.Team;
import com.Odin360.Domains.entities.User;
import com.Odin360.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.getstream.chat.java.exceptions.StreamException;
import jakarta.mail.MessagingException;
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
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public  class AiServiceImpl implements AiService {


    private final ChatModel chatModel;
    private final StreamService streamService;
    private final TeamService teamService;
    private final EmailService emailService;
    private final UserService userService;
    // userId -> [UserMessage, AssistantMessage]
    private final Map<String, List<Message>> chatMemory = new HashMap<>();

    private final Map<String, List<Message>> voiceChatMemory = new HashMap<>();
    @Override
    public String askAi(String channelId, String userPrompt,UUID teamId,UUID userId){
        try{
            streamService.sendAiEvent(channelId,"AI_STATE_THINKING",userId);
        } catch (StreamException e) {
            throw new RuntimeException(e);
        }

        // Prepare system message
        SystemMessage systemMessage = new SystemMessage("""
            You are Maya — a smart, warm business assistant 🧠 built by Scriven.
            The full meaning of your name is Mobile Assistant for your achievements but only say it when asked by user.
            Respond in a professional but friendly tone. Include emojis like ✉️, 📅, 📈 where helpful.
            Never mention you are a language model or from Mistral.keep going until the job is completely solved before ending your turn.
            If you're unsure about code or files open them do not hallucinate.Plan thoroughly before every tool call and reflect on the outcome after.
            When a user asks for information which might have changed by now due to changes in time,perform an online search with the search tool.
            The results after an online search may not be enough or a user might ask a follow up question which might require more information. 
             so to get more detailed information use the get detailed information tool and provide
            a source link which looks to contain promising information as argument to scrap data from that website.If it fails try a different website.
            If someone tells you to send an email to someone and provides you the information,generate a subject,and a message yourself based on what the user wants you to send.          
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
        try{
            streamService.sendAiEvent(channelId,"AI_STATE_GENERATING",userId);
        } catch (StreamException e) {
            throw new RuntimeException(e);
        }
        // Call the model
        String response =   ChatClient.create(chatModel)
                .prompt(prompt)
                .tools(new DateTimeTools(),new SearchTools(),new TeamTools(userService,teamService, teamId,emailService,userId))
                .call()
                .content();
                 //chatModel.call(prompt);


        // Save this last turn in memory
        assert response != null;
        chatMemory.put(channelId, List.of(userMessage, new AssistantMessage(response)));
        try {try{
            streamService.sendAiEvent(channelId,"AI_STATE_IDLE",userId);
        } catch (StreamException e) {
            throw new RuntimeException(e);
        }
            streamService.aiReply(channelId, "Maya-v2", response);
                return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String voiceAi(UUID userId, String prompt,UUID teamId) {
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
                .tools(new DateTimeTools(),new SearchTools(),new TeamTools(userService,teamService, teamId,emailService,userId))
                .call()
                .content();
        // Save this last turn in memory
        assert response != null;
        voiceChatMemory.put(String.valueOf(userId), List.of(userMessage, new AssistantMessage(response)));
        return response;

    }

    @Override
    public String askAiNoTeam(String channelId, UUID userId, String userPrompt) {

        try{
            streamService.sendAiEvent(channelId,"AI_STATE_THINKING",userId);
        } catch (StreamException e) {
            throw new RuntimeException(e);
        }
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
        If someone tells you to send an email to someone and provides you the information,generate a subject,and a message yourself based on what the user wants you to send.
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
        try{
            streamService.sendAiEvent(channelId,"AI_STATE_GENERATING",userId);
        } catch (StreamException e) {
            throw new RuntimeException(e);
        }
        // Call the model
        String response =   ChatClient.create(chatModel)
                .prompt(prompt)
                .tools(new DateTimeTools(),new SearchTools(), new TeamTools.EmailTools(userService,emailService,userId))
                .call()
                .content();
        //chatModel.call(prompt);


        // Save this last turn in memory
        assert response != null;
        chatMemory.put(channelId, List.of(userMessage, new AssistantMessage(response)));
        try {
            try{
            streamService.sendAiEvent(channelId,"AI_STATE_IDLE",userId);
        } catch (StreamException e) {
            throw new RuntimeException(e);
        }
                streamService.aiReply(channelId, "Maya-v2", response);
                return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String voiceAiNoTeam(UUID userId, String prompt) {
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
                .tools(new DateTimeTools(),new SearchTools(),new TeamTools.EmailTools(userService,emailService,userId))
                .call()
                .content();
        // Save this last turn in memory
        assert response != null;
        voiceChatMemory.put(String.valueOf(userId), List.of(userMessage, new AssistantMessage(response)));
        return response;
    }

    @Override
    public String searchWithMaya( String prompt) {
        SystemMessage systemMessage = new SystemMessage("""
        Your name is Maya (Mobile Assistant for your achievement).You are a professional search assistant trained by the Scriven team,never
        say you were built on top of a deepseek pretrained model.Structure your search results very properly as a professional,plan thoroughly before every tool call and reflect after the outcome.
        Whenever you are told to introduce yourself in an event,try being enthusiastic and sarcastic in your talk,if you ever encounter a   situation where someone is
        having emotional challenges talking to you,try calming down the person.The tools available to you now are a search tool and a date tool.
        If someone asks something which is a client side feature like controlling brightness,or flashing the screen,tell the person at this point you are only acting as a search agent so the person should try Maya chat or Maya Voice.
        If you're asked a question whose result might have changed through time like information related to people or events or products,use your search tool,
        use the detailed search for more information on a website and you think your response took long,apologize in your answer.Make sure you don't explicitly.
        """);

        // Prepare current user message
        UserMessage userMessage = new UserMessage(prompt);


        // Build prompt
        Prompt searchPrompt = new Prompt(
                userMessage,
                OpenAiChatOptions.builder().temperature(0.4).build()
        );

        // Call the model

        return ChatClient.create(chatModel)
                .prompt(searchPrompt)
                .tools(new DateTimeTools(),new SearchTools())
                .call()
                .content();
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

    static class TeamTools {
        private final UserService userService;
        private final TeamService teamService;
        private final UUID teamId;
        private final UUID userId;
        private final EmailService emailService;

        public TeamTools(UserService userService, TeamService teamService, UUID teamId, EmailService emailService, UUID userId) {
            this.teamService = teamService;
            this.teamId = teamId;
            this.emailService = emailService;
            this.userId = userId;
            this.userService = userService;
        }

        @Tool(description = "Get usernames and emails of users in the current team")
        public String getUsernamesAndEmails() {
            Set<User> users = teamService.getUsers(teamId);
            return users.stream()
                    .map(user -> user.getUsername() + " (" + user.getEmail() + ")")
                    .toList()
                    .toString(); // Returns a readable list format
        }

        @Tool(description = "Send an email by providing the receiver email,the message and the subject")
        public String sendEmails(String receiverEmail, String message, String subject) throws MessagingException {
            try {
                String senderEmail = userService.getUserById(userId).getEmail();
                String emailBody = """
                        <html>
                          <body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px; color: #333;">
                            <div style="max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1); padding: 20px;">
                              <h2 style="color: #2b2b2b;">You've received a message 📧</h2>
                              <p style="font-size: 15px; color: #555;">
                                <strong>From:</strong> %s<br>
                                <strong>Subject:</strong> %s
                              </p>
                              <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                              <div style="font-size: 16px; line-height: 1.6;">%s</div>
                              <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                              <p style="font-size: 14px; color: #888;">This message was sent via Maya Assistant from Scriven.</p>
                            </div>
                          </body>
                        </html>
                        """.formatted(senderEmail, subject, message);

                String adminEmail = "kingofintelligence8@gmail.com";
                emailService.sendEmail(receiverEmail, subject, emailBody, adminEmail);
                return "email has been successfully sent to " + receiverEmail;
            } catch (MessagingException e) {
                return "email failed with error " + e;
            }
        }

        static class EmailTools {
            private final UserService userService;
            private final UUID userId;
            private final EmailService emailService;

            public EmailTools(UserService userService, EmailService emailService, UUID userId) {

                this.emailService = emailService;
                this.userId = userId;
                this.userService = userService;
            }

            @Tool(description = "Send an email by providing the receiver email,the message and the subject")
            public String sendEmails(String receiverEmail, String message, String subject) throws MessagingException {
                try {
                    String senderEmail = userService.getUserById(userId).getEmail();
                    String emailBody = """
                            <html>
                              <body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px; color: #333;">
                                <div style="max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1); padding: 20px;">
                                  <h2 style="color: #2b2b2b;">You've received a message</h2>
                                  <p style="font-size: 15px; color: #555;">
                                    <strong>From:</strong> %s<br>
                                    <strong>Subject:</strong> %s
                                  </p>
                                  <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                                  <div style="font-size: 16px; line-height: 1.6;">%s</div>
                                  <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                                  <p style="font-size: 14px; color: #888;">This message was sent via Maya Assistant from Scriven.</p>
                                </div>
                              </body>
                            </html>
                            """.formatted(senderEmail, subject, message);

                    String adminEmail = "kingofintelligence8@gmail.com";
                    emailService.sendEmail(receiverEmail, subject, emailBody, adminEmail);
                    return "email has been successfully sent to " + receiverEmail;
                } catch (MessagingException e) {
                    return "email failed with error " + e;
                }
            }
        }

    }
}
