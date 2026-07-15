package org.myspring.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient githubRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.github.com")
                .build();
    }

}
