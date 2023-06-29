package com.twitchforge.util;

import com.twitchforge.exception.BadResponseException;
import com.twitchforge.exception.InvalidUrlException;
import com.twitchforge.model.Feeds;
import com.twitchforge.model.enums.Quality;
import com.twitchforge.model.response.Data;
import com.twitchforge.model.response.TwitchTrackerResponse;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.twitchforge.model.enums.Quality.*;
import static com.twitchforge.util.Domains.domains;

public class Parser {

    public static Feeds parseTwitchResponse(Data data, RestTemplate restTemplate) {
        String vodToken = parseVodToken(data.getThumbnailUrl());
        String domain = getValidDomain(vodToken, restTemplate);
        return populateFeeds(domain, vodToken, restTemplate);
    }

    public static TwitchTrackerResponse getTwitchTrackerData(String url, RestTemplate restTemplate) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0");
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new InvalidUrlException();
        }

        TwitchTrackerResponse twitchTrackerResponse = new TwitchTrackerResponse();

        try (BufferedReader brt = new BufferedReader(new StringReader(response.getBody()))) {
            String line;
            for (int i = 0; i < 300; i++) {
                line = brt.readLine();
                if (i == 7) {
                    int tsIndex = line.indexOf(" on ") + 4;
                    twitchTrackerResponse.setStartTime(line.substring(tsIndex, tsIndex + 19));
                }
            }
            twitchTrackerResponse.setStreamerUsername(parseStreamerNickname(url));
            twitchTrackerResponse.setStreamId(parseStreamId(url));
        } catch (IOException e) {
            throw new BadResponseException();
        }

        return twitchTrackerResponse;
    }

    public static Feeds parseTwitchTrackerResponse(TwitchTrackerResponse response, RestTemplate restTemplate) {
        long timestamp = getUNIX(response.getStartTime());
        String vodToken = computeVodToken(response.getStreamerUsername(), response.getStreamId(), timestamp);
        String domain = getValidDomain(vodToken, restTemplate);
        return populateFeeds(domain, vodToken, restTemplate);
    }

    private static String parseVodToken(String thumbnailUrl) {
        Pattern pattern = Pattern.compile("/([a-zA-Z0-9_]+)//thumb");
        Matcher matcher = pattern.matcher(thumbnailUrl);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new BadResponseException();
        }
    }

    private static String getValidDomain(String vodToken, RestTemplate restTemplate) {
        String m3u8Link = SOURCE.getM3u8Link();
        Optional<String> validDomain = domains.parallelStream()
                .filter(domain -> {
                    String fullUrl = domain + vodToken + m3u8Link;
                    try {
                        ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);
                        return response.getStatusCode().is2xxSuccessful();
                    } catch (HttpClientErrorException ex) {
                        return false;
                    }
                })
                .findFirst();

        return validDomain.orElseThrow(BadResponseException::new);
    }

    private static Feeds populateFeeds(String domain, String vodToken, RestTemplate restTemplate) {
        Map<String, Quality> feedsMap = new ConcurrentHashMap<>(Quality.values().length);
        String baseUrl = domain + vodToken;
        Arrays.stream(Quality.values()).parallel().forEach(quality -> {
            String fullUrl = baseUrl + quality.getM3u8Link();
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    feedsMap.put(baseUrl + quality.getM3u8Link(), quality);
                }
            } catch (HttpClientErrorException ignored) {}
        });
        return new Feeds(feedsMap);
    }

    private static long getUNIX(String timestamp) {
        String time = timestamp + " UTC";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz");
        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }

    private static String computeVodToken(String streamerUsername, long streamId, long timestamp) {
        String token = streamerUsername + "_" + streamId + "_" + timestamp;
        String hash = hash(token);
        return hash + "_" + token;
    }

    private static String hash(String token) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ignored) {}
        byte[] result = md.digest(token.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte val : result) {
            sb.append(String.format("%02x", val));
        }
        return sb.substring(0, 20);
    }

    private static String parseStreamerNickname(String url) {
        Pattern pattern = Pattern.compile(".+/(.+)/streams/\\d+");
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            throw new InvalidUrlException();
        }
    }

    private static long parseStreamId(String url) {
        Pattern pattern = Pattern.compile(".+/streams/(\\d+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            return Long.parseLong(matcher.group(1));
        } else {
            throw new InvalidUrlException();
        }
    }
}
