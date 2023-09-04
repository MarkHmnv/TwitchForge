package com.twitchforge;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.twitchforge.config.ConfigLoader;
import com.twitchforge.model.request.TokenRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import java.security.MessageDigest;

import static com.twitchforge.util.Constants.*;

@Slf4j
public class TwitchForgeModule extends AbstractModule {
    @Provides
    public TokenRequest tokenRequest(ConfigLoader config) {
        return TokenRequest.builder()
                .clientId(config.getString(CLIENT_ID))
                .clientSecret(config.getString(CLIENT_SECRET))
                .grantType(GRANT_TYPE)
                .build();
    }

    @Provides
    @SneakyThrows
    MessageDigest shaDigest() {
        return MessageDigest.getInstance("SHA-1");
    }

    @Provides
    HttpHeaders headersWithUserAgent() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0");
        return headers;
    }
}