package org.myspring.backend.service.rag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.factory.ChatClientFactory;
import org.myspring.backend.model.UserSetting;
import org.myspring.backend.service.ApiKeyEncryptionService;
import org.myspring.backend.service.UserSettingService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TitleGeneratorServiceTest {

    @Mock
    private ChatClientFactory chatClientFactory;

    @Mock
    private UserSettingService userSettingService;

    @Mock
    private ApiKeyEncryptionService apiKeyEncryptionService;

    @Mock(answer = Answers.RETURNS_SELF)
    private ChatClient.Builder builder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private static final String MODEL = "gpt-4o-mini";

    private TitleGeneratorService titleGeneratorService;

    @BeforeEach
    void setUp() {
        titleGeneratorService = new TitleGeneratorService(
                chatClientFactory,
                userSettingService,
                apiKeyEncryptionService
        );
        ReflectionTestUtils.setField(titleGeneratorService, "model", MODEL);
    }

    @Test
    void generate_returnsTitleFromChatClientResponse() {
        UserSetting userSetting = UserSetting.builder().apiKey("encrypted-key").build();
        when(userSettingService.findByUserId(1L)).thenReturn(userSetting);
        when(apiKeyEncryptionService.decrypt("encrypted-key")).thenReturn("sk-test-key");
        when(chatClientFactory.createChatClientBuilder("sk-test-key", MODEL)).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user("How do I make rendang?")).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Rendang Cooking Help");

        String title = titleGeneratorService.generate(1L, "How do I make rendang?");

        assertThat(title).isEqualTo("Rendang Cooking Help");
    }
}
