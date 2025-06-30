package com.Odin360.services.impl;

import com.Odin360.services.StreamService;
import io.getstream.chat.java.exceptions.StreamException;
import io.getstream.chat.java.models.Message;
import io.getstream.chat.java.models.User;
import io.getstream.chat.java.services.framework.Client;
import io.getstream.chat.java.services.framework.DefaultClient;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.GregorianCalendar;
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

    @Override
    public String clientToken(String userId) {
        try{
        var calendar = new GregorianCalendar();
        calendar.add(Calendar.HOUR, 1);
        return User.createToken(userId, calendar.getTime(), null);} catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }
}
