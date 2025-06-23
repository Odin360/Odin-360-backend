package com.Odin360.configuration;

import io.getstream.chat.java.services.framework.Client;
import io.getstream.chat.java.services.framework.DefaultClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class StreamClientConfig {
    @Value("${io.getstream.chat.apiKey}")
    private String apikey;
    @Value("${io.getstream.chat.apiSecret}")
    private String apiSecret;
    @Bean
    public DefaultClient StreamClient(){
        var properties = new Properties();
        properties.put(DefaultClient.API_KEY_PROP_NAME, apikey);
        properties.put(DefaultClient.API_SECRET_PROP_NAME, apiSecret);
        var client = new DefaultClient(properties);
        DefaultClient.setInstance(client);
        return  client;
    }
}
