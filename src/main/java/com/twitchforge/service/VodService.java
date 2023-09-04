package com.twitchforge.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.twitchforge.config.ConfigLoader;
import com.twitchforge.exception.InvalidUrlException;
import com.twitchforge.model.enums.Quality;
import com.twitchforge.model.request.TokenRequest;
import com.twitchforge.model.response.TokenResponse;
import com.twitchforge.model.response.TwitchTrackerResponse;
import com.twitchforge.model.response.VodResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

import static com.twitchforge.util.Constants.*;

@Singleton
public class VodService {
    private final RestTemplate restTemplate;
    private final TokenRequest tokenRequest;
    private final ParserService parserService;
    private final ConfigLoader config;
    private TokenResponse token;

    @Inject
    public VodService(ParserService parserService,
                      TokenRequest tokenRequest,
                      RestTemplate restTemplate,
                      ConfigLoader config) {
        this.parserService = parserService;
        this.tokenRequest = tokenRequest;
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public Map<String, Quality> retrieveVod(String vodUrl) {
        String vodId = extractVodId(vodUrl).orElseThrow(InvalidUrlException::new);
        String url = VOD_RETRIEVE_URL + vodId;
        updateBearerToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.getAccessToken());
        headers.set(CLIENT_ID_HEADER, config.getString(CLIENT_ID));
        ResponseEntity<VodResponse> vodResponse = restTemplate
                .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), VodResponse.class);

        if (!vodResponse.getStatusCode().is2xxSuccessful()) {
            throw new InvalidUrlException();
        }
        return parserService.parseTwitchResponse(vodResponse.getBody().getData().get(0), restTemplate);
    }

    public Map<String, Quality> recoverVod(String vodUrl) {
        TwitchTrackerResponse response = parserService.getTwitchTrackerData(vodUrl, restTemplate);
        return parserService.parseTwitchTrackerResponse(response, restTemplate);
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

    private Optional<String> extractVodId(String url) {
        Optional<String> videoId = Optional.empty();
        Matcher matcher = TWITCH_VIDEO_PATTERN.matcher(url);

        if (matcher.find()) {
            videoId = Optional.of(matcher.group(1));
        }

        return videoId;
    }
}
