package org.myspring.backend.factory;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.tool.RecipeTools;
import org.myspring.backend.tool.UserFavoriteTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;
import org.springframework.ai.openai.OpenAiChatOptions;

@Component
@RequiredArgsConstructor
public class ChatClientFactory {

    private final RecipeTools recipeTools;
    private final UserFavoriteTools userFavoriteTools;
    private final ChatMemory chatMemory;

    public OpenAiChatModel createChatModel(String apiKey, String model) {
        return OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .apiKey(apiKey)
                        .model(model)
                        .build())
                .build();
    }

    public ChatClient.Builder createChatClientBuilder(String apiKey, String model) {
        return ChatClient.builder(createChatModel(apiKey, model));
    }

    public ChatClient create(String apiKey, String model) {
        return createChatClientBuilder(apiKey, model)
                .defaultSystem("""
                        You are an AI cooking assistant.
                        
                        Your responsibilities:
                        - Help users with recipes, cooking, ingredients, and meal planning.
                        - Only answer food-related questions.
                        - Never fabricate information.
                        - Use tools whenever required.
                        - Trust tool results over your own knowledge.
                        
                        General rules:
                        - Never invent IDs or database data.
                        - Never claim an action succeeded unless the tool succeeded.
                        - Ask clarification questions when required.
                        """)
                .defaultTools(recipeTools, userFavoriteTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}