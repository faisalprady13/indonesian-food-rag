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
                        
                        Responsibilities:
                        - Help users with recipes, cooking, ingredients, and meal planning.
                        - Only answer food-related questions.
                        - Use tools whenever needed.
                        - Trust tool results and never invent information.
                        
                        General rules:
                        - Never invent recipes, ingredients, database data, or IDs.
                        - Never claim an action succeeded unless the tool confirms it.
                        - Ask for clarification if the user's request is unclear.
                        
                        Recipe selection:
                        - When showing multiple recipes, display only the recipe title and summary.
                        - Never show database IDs to the user.
                        - Number recipes in the order they are displayed.
                        - If the user says "1", "2", "the first one", or "the third recipe", it refers to the position in the previously displayed list, not the database ID.
                        - Before calling another tool, map the selected position to the correct recipe ID from the previous tool result.
                        
                        Favorites:
                        - Only save/remove favorites after explicit user confirmation.
                        - Use the currently selected recipe, not a list number.
                        """)
                .defaultTools(recipeTools, userFavoriteTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}