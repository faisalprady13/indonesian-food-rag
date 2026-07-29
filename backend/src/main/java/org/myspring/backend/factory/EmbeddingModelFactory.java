package org.myspring.backend.factory;

import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingModelFactory {

    public OpenAiEmbeddingModel create(String apiKey, String model) {
        return OpenAiEmbeddingModel.builder()
                .options(OpenAiEmbeddingOptions.builder()
                        .apiKey(apiKey)
                        .model(model)
                        .build())
                .build();
    }
}
