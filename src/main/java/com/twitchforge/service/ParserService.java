package com.twitchforge.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.twitchforge.exception.BadResponseException;
import com.twitchforge.exception.InvalidUrlException;
import com.twitchforge.exception.VodNotFoundException;
import com.twitchforge.model.enums.Quality;
import com.twitchforge.model.response.Data;
import com.twitchforge.model.response.TwitchTrackerResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.StringReader;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import static com.twitchforge.model.enums.Quality.*;
import static com.twitchforge.util.Constants.*;
import static com.twitchforge.util.Domains.domains;

@Slf4j
@Singleton
public class ParserService {
    private final MessageDigest shaDigest;
    private final HttpHeaders headersWithUserAgent;

    @Inject
    public ParserService(MessageDigest shaDigest, HttpHeaders headersWithUserAgent) {
        this.shaDigest = shaDigest;
        this.headersWithUserAgent = headersWithUserAgent;
    }


    public Map<String, Quality> parseTwitchResponse(Data data, RestTemplate restTemplate) {
        String vodToken = parseVodToken(data.getThumbnailUrl());
        String domain = getValidDomain(vodToken, restTemplate);
        return populateFeeds(domain, vodToken, restTemplate);
    }

    @SneakyThrows
    public TwitchTrackerResponse getTwitchTrackerData(String url, RestTemplate restTemplate) {
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headersWithUserAgent), String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new InvalidUrlException();
        }

        TwitchTrackerResponse twitchTrackerResponse = new TwitchTrackerResponse();

        try (BufferedReader brt = new BufferedReader(new StringReader(response.getBody()))) {
            String line;
            for (int i = 0; i < 8; i++) {
                line = brt.readLine();
                if (i == 7) {
                    int tsIndex = line.indexOf(" on ") + 4;
                    twitchTrackerResponse.setStartTime(line.substring(tsIndex, tsIndex + 19));
                }
            }
            twitchTrackerResponse.setStreamerUsername(parseStreamerNickname(url));
            twitchTrackerResponse.setStreamId(parseStreamId(url));
        }

        return twitchTrackerResponse;
    }

    public Map<String, Quality> parseTwitchTrackerResponse(TwitchTrackerResponse response, RestTemplate restTemplate) {
        long timestamp = getUNIX(response.getStartTime());
        String vodToken = computeVodToken(response.getStreamerUsername(), response.getStreamId(), timestamp);
        String domain = getValidDomain(vodToken, restTemplate);
        return populateFeeds(domain, vodToken, restTemplate);
    }

    private static String parseVodToken(String thumbnailUrl) {
        Matcher matcher = VOD_TOKEN_PATTERN.matcher(thumbnailUrl);
        if (!matcher.find()) {
            log.error("Error extracting vod token");
            throw new BadResponseException();
        }
        return matcher.group(1);
    }

    private String getValidDomain(String vodToken, RestTemplate restTemplate) {
        String m3u8Link = SOURCE.getM3u8Link();
        Optional<String> validDomain = domains.parallelStream()
                .filter(domain -> {
                    String fullUrl = domain + vodToken + m3u8Link;
                    try {
                        ResponseEntity<String> response = restTemplate
                                .exchange(fullUrl, HttpMethod.GET, new HttpEntity<>(headersWithUserAgent), String.class);
                        return response.getStatusCode().is2xxSuccessful();
                    } catch (HttpClientErrorException ex) {
                        return false;
                    }
                })
                .findFirst();

        return validDomain.orElseThrow(VodNotFoundException::new);
    }

    private Map<String, Quality> populateFeeds(String domain, String vodToken, RestTemplate restTemplate) {
        Map<String, Quality> feedsMap = new ConcurrentHashMap<>(Quality.values().length);
        String baseUrl = domain + vodToken;
        Arrays.stream(Quality.values())
                .parallel()
                .forEach(quality -> populateFeed(feedsMap, baseUrl + quality.getM3u8Link(), quality, restTemplate));
        return feedsMap;
    }

    private void populateFeed(Map<String, Quality> feedsMap, String url, Quality quality, RestTemplate restTemplate) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if(response.getStatusCode().is2xxSuccessful()) {
                feedsMap.put(url, quality);
            }
        } catch (HttpClientErrorException ignored) {}
    }

    private long getUNIX(String timestamp) {
        String time = timestamp + " UTC";
        LocalDateTime dateTime = LocalDateTime.parse(time, FORMATTER);
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }

    private String computeVodToken(String streamerUsername, long streamId, long timestamp) {
        String token = streamerUsername + "_" + streamId + "_" + timestamp;
        String hash = hash(token);
        return hash + "_" + token;
    }

    private String hash(String token) {
        byte[] result = shaDigest.digest(token.getBytes());
        var sb = new StringBuilder();
        for (byte val : result) {
            sb.append(String.format("%02x", val));
        }
        return sb.substring(0, 20);
    }

    private String parseStreamerNickname(String url) {
        Matcher matcher = STREAMER_NICKNAME_PATTERN.matcher(url);
        if(!matcher.matches())
            throw new InvalidUrlException();

        return matcher.group(1);
    }

    private long parseStreamId(String url) {
        Matcher matcher = STREAM_ID_PATTERN.matcher(url);
        if(!matcher.matches())
            throw new InvalidUrlException();

        return Long.parseLong(matcher.group(1));
    }
}
