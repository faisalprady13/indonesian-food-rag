package org.myspring.backend.factory;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Component
public class EmbeddingModelFactory {

    // Keyed by "apiKey|model" so the underlying HTTP client to OpenAI is reused across
    // tool invocations instead of rebuilt on every searchRecipes call.
    private final ConcurrentMap<String, OpenAiEmbeddingModel> embeddingModelCache = Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .<String, OpenAiEmbeddingModel>build()
            .asMap();

    public OpenAiEmbeddingModel create(String apiKey, String model) {
        return embeddingModelCache.computeIfAbsent(
                apiKey + "|" + model,
                key -> OpenAiEmbeddingModel.builder()
                        .options(OpenAiEmbeddingOptions.builder()
                                .apiKey(apiKey)
                                .model(model)
                                .build())
                        .build()
        );
    }
}
