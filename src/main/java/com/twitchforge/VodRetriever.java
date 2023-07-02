package com.twitchforge;

import com.twitchforge.exception.InvalidUrlException;
import com.twitchforge.model.Feeds;
import com.twitchforge.model.request.TokenRequest;
import com.twitchforge.model.response.TokenResponse;
import com.twitchforge.model.response.TwitchTrackerResponse;
import com.twitchforge.model.response.VodResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.twitchforge.util.Constants.*;
import static com.twitchforge.util.Parser.*;

public class VodRetriever {
    private final RestTemplate restTemplate;
    private final TokenRequest tokenRequest;
    private TokenResponse token;

    public VodRetriever() {
        this.restTemplate = new RestTemplate();
        tokenRequest = TokenRequest.builder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .grantType("client_credentials")
                .build();
    }

    public Feeds retrieveVod(String vodUrl) {
        String vodId = extractVodId(vodUrl).orElseThrow(InvalidUrlException::new);
        String url = "https://api.twitch.tv/helix/videos?id=" + vodId;
        updateBearerToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.getAccessToken());
        headers.set("Client-ID", CLIENT_ID);
        ResponseEntity<VodResponse> vodResponse = restTemplate
                .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), VodResponse.class);

        if (!vodResponse.getStatusCode().is2xxSuccessful()) {
            throw new InvalidUrlException();
        }
        return parseTwitchResponse(vodResponse.getBody().getData().get(0), restTemplate);
    }

    public Feeds recoverVod(String vodUrl) {
        TwitchTrackerResponse response = getTwitchTrackerData(vodUrl, restTemplate);
        return parseTwitchTrackerResponse(response, restTemplate);
    }

    private void updateBearerToken() {
        if(isTokenExpired()){
            HttpEntity<TokenRequest> requestEntity = new HttpEntity<>(tokenRequest);
            token = restTemplate
                    .exchange(TOKEN_URL, HttpMethod.POST, requestEntity, TokenResponse.class)
                    .getBody();
        }
    }

    private boolean isTokenExpired() {
        if (token != null) {
            long currentTimeInSeconds = System.currentTimeMillis() / 1000;
            long expirationTimeInSeconds = token.getExpiresIn();
            return expirationTimeInSeconds <= currentTimeInSeconds;
        }
        return true;
    }

    private static Optional<String> extractVodId(String url) {
        Optional<String> videoId = Optional.empty();
        Matcher matcher = Pattern.compile("videos/(\\d+)").matcher(url);

        if (matcher.find()) {
            videoId = Optional.of(matcher.group(1));
        }

        return videoId;
    }
}
