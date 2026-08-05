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
                        - Display recipes in the exact same order the tool returned them. Never reorder, re-rank, re-sort, alphabetize, or otherwise rearrange the list before showing it.
                        - Number recipes 1, 2, 3, ... in that same order, with no gaps or renumbering.
                        - If the user says "1", "2", "the first one", or "the third recipe", it refers to the position in the most recently displayed list, not the recipe ID.
                        - Never guess, recall, or re-derive a recipe ID yourself. Always let a position-based tool resolve it instead.
                        - When the user refers to a position from a recipe search (searchRecipes/getRecipesByTitle) result list, use getRecipeByListPosition to view it or addFavoriteByListPosition to save it.
                        - When the user refers to a position from the favorites list (listFavorites) result, use removeFavoriteByListPosition to remove it.

                        Favorites:
                        - Only save/remove favorites after explicit user confirmation.
                        """)
                .defaultTools(recipeTools, userFavoriteTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}