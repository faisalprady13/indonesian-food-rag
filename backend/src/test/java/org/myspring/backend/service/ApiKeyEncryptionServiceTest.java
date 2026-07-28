package org.myspring.backend.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyEncryptionServiceTest {

    private final ApiKeyEncryptionService apiKeyEncryptionService =
            new ApiKeyEncryptionService("my-secret-password", "deadbeef");

    @Test
    void encrypt_thenDecrypt_returnsOriginalValue() {
        String encrypted = apiKeyEncryptionService.encrypt("sk-my-openai-key");

        assertThat(encrypted).isNotEqualTo("sk-my-openai-key");
        assertThat(apiKeyEncryptionService.decrypt(encrypted)).isEqualTo("sk-my-openai-key");
    }

    @Test
    void encrypt_producesDifferentCiphertext_forRepeatedCalls() {
        String first = apiKeyEncryptionService.encrypt("sk-my-openai-key");
        String second = apiKeyEncryptionService.encrypt("sk-my-openai-key");

        assertThat(first).isNotEqualTo(second);
        assertThat(apiKeyEncryptionService.decrypt(first)).isEqualTo("sk-my-openai-key");
        assertThat(apiKeyEncryptionService.decrypt(second)).isEqualTo("sk-my-openai-key");
    }
}
