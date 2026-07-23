package org.myspring.backend.service.rag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TitleGeneratorServiceTest {

    @Mock(answer = Answers.RETURNS_SELF)
    private ChatClient.Builder builder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private TitleGeneratorService titleGeneratorService;

    @BeforeEach
    void setUp() {
        when(builder.build()).thenReturn(chatClient);
        titleGeneratorService = new TitleGeneratorService(builder);
    }

    @Test
    void generate_returnsTitleFromChatClientResponse() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user("How do I make rendang?")).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Rendang Cooking Help");

        String title = titleGeneratorService.generate("How do I make rendang?");

        assertThat(title).isEqualTo("Rendang Cooking Help");
    }
}
