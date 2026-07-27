package org.myspring.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class ApiKeyEncryptionService {

    private final TextEncryptor encryptor;

    public ApiKeyEncryptionService(
            @Value("${app.encryption.password}") String password,
            @Value("${app.encryption.salt}") String salt
    ) {
        this.encryptor = Encryptors.text(password, salt);
    }

    public String encrypt(String apiKey) {
        return encryptor.encrypt(apiKey);
    }

    public String decrypt(String encryptedApiKey) {
        return encryptor.decrypt(encryptedApiKey);
    }
}