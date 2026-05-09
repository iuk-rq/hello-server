package com.stu.helloserver.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.stu.helloserver.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;

    public ChatServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("你是友好简洁的智能助手")
                .defaultOptions(DashScopeChatOptions.builder()
                        .withTopP(0.7)
                        .build())
                .build();
    }

    @Override
    public String chat(String message) {
        return chatClient.prompt(message).call().content();
    }
}
