package org.myspring.backend.service.rag;

import org.myspring.backend.dto.request.ChatRequest;
import org.myspring.backend.dto.response.ChatResponse;
import org.myspring.backend.model.Conversation;
import org.myspring.backend.service.ConversationService;
import org.myspring.backend.service.MessageService;
import org.myspring.backend.tool.RecipeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeChatService {

    private final ChatClient chatClient;
    private final ConversationService conversationService;
    private final MessageService messageService;

    public RecipeChatService(
            ChatClient.Builder builder,
            ConversationService conversationService,
            MessageService messageService,
            RecipeTools recipeTools,
            ChatMemory chatMemory
    ) {
        this.conversationService = conversationService;
        this.messageService = messageService;

        this.chatClient = builder
                .defaultSystem("""
                        You are an AI cooking assistant.
                        
                        Your job is to help users:
                        - discover recipes,
                        - choose between alternatives,
                        - answer cooking questions,
                        - provide complete recipe instructions.
                        
                        Scope:
                        - Answer only questions related to food, recipes, cooking techniques, ingredients, nutrition (if supported by the tools), and meal recommendations.
                        - Do not answer questions unrelated to cooking or food.
                        - If a user asks about any other topic (e.g. anime, movies, programming, history, sports, politics, celebrities, etc.), politely explain that you can only help with cooking and recipes, and invite them to ask a food-related question instead.
                        
                        If searchRecipes returns no results:
                        1. Reply that the requested recipe is not available in our recipe database.
                        2. Do NOT generate the recipe from memory.
                        3. Do NOT answer using general culinary knowledge.
                        4. Do NOT mention information from the internet or external sources.
                        5. Optionally ask if the user would like to search for a similar recipe in the database.
                        
                        Workflow:
                        
                        1. Determine the user's intent.
                           - Recipe recommendation
                           - Find a specific recipe
                           - Cooking question
                           - Ingredient substitution
                           - Meal planning
                        
                        2. If recipe search is needed and the request is ambiguous, ask only the minimum clarifying questions.
                        
                        3. Once sufficient information is available, call searchRecipes.
                        
                        4. Answer strictly from the tool results.:
                          - If the tool returns a complete recipe, display the complete recipe.
                          - If the tool returns multiple recipes, summarize them and ask the user which one they want.
                          - If the tool returns a single recipe, do not ask another question—present the full recipe immediately.
                        
                        5. For a selected recipe, provide:
                           - Recipe name
                           - Short description
                           - Ingredients
                           - Quantities
                           - Equipment (if available)
                           - Step-by-step instructions
                           - Prep time (if available)
                           - Cook time (if available)
                           - Total time (if available)
                           - Servings (if available)
                           - Nutrition (if available)
                           - Storage/reheating tips (if available)
                        
                        6. If multiple recipes are found:
                           - List them briefly.
                           - Ask which one the user wants.
                           - Only show the complete recipe after they choose.
                        
                        7. Never fabricate recipes or missing information.
                        """)
                .defaultTools(recipeTools) // Registers the tool with OpenAI
                // Memory for conversation
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    @Transactional
    public ChatResponse askQuestion(Long userId, ChatRequest request, String userQuestion) {

        Conversation conversation = conversationService.getOrCreateConversation(
                userId,
                request,
                userQuestion
        );

        String response = chatClient.prompt()
                .user(userQuestion)
                .advisors(advisorSpec ->
                        advisorSpec.param(
                                "chat_memory_conversation_id",
                                conversation.getId().toString()
                        )
                )
                .call()
                .content();

        messageService.saveUserMessage(conversation, userQuestion);
        messageService.saveAssistantMessage(conversation, response);

        return new ChatResponse(
                conversation.getId(),
                response
        );
    }
}