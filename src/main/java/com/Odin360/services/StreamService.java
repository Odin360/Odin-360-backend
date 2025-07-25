package com.Odin360.services;

import io.getstream.chat.java.exceptions.StreamException;

import java.util.UUID;

public interface StreamService {
    void aiReply(String channelId,String aiId,String assistantReply) throws StreamException;
    String clientToken(String userId);
    void sendAiEvent(String channelId,String aiState,UUID userId) throws StreamException;
}
