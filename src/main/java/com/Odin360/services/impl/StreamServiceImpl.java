package com.Odin360.services.impl;

import com.Odin360.services.StreamService;
import io.getstream.chat.java.exceptions.StreamException;
import io.getstream.chat.java.models.Message;
import io.getstream.chat.java.services.framework.Client;
import io.getstream.chat.java.services.framework.DefaultClient;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.UUID;


@Service
@Builder
@RequiredArgsConstructor
public class StreamServiceImpl implements StreamService{
    private final DefaultClient streamClient;

    @Override
    public void aiReply(String channelId, String aiId,String assistantReply) throws StreamException{
        var message =  Message.MessageRequestObject.builder()
                .text(assistantReply)
                .userId(aiId)
                .build();

        io.getstream.chat.java.models.Message.send("messaging", channelId)
                .message(message)
                .request();

    }


}
