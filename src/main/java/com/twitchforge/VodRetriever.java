package com.twitchforge;

import com.twitchforge.exception.BadResponseException;
import com.twitchforge.exception.InvalidUrlException;
import com.twitchforge.model.*;
import com.twitchforge.model.Extensions;
import com.twitchforge.model.request.PlaybackAccessTokenRequest;
import com.twitchforge.model.request.Variables;
import com.twitchforge.model.response.PlaybackAccessTokenResponse;
import com.twitchforge.util.FileUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.twitchforge.util.Constants.*;
import static com.twitchforge.util.Parser.parseFeeds;

public class VodRetriever {
    private final RestTemplate restTemplate;
    private final Extensions extensions;
    public VodRetriever() {
        this.restTemplate = new RestTemplate();
        PersistedQuery persistedQuery = PersistedQuery.builder()
                .version(1)
                .sha256Hash(HASH)
                .build();
        extensions = new Extensions(persistedQuery);
    }
    public Feeds getFeeds(String vodUrl) {
        String vodId = extractVodId(vodUrl).orElseThrow(InvalidUrlException::new);
        Token token = retrieveToken(vodId);
        String url = "https://usher.ttvnw.net/vod/" + vodId + ".m3u8?sig=" + token.getSignature() +
                "&token=" + token.getToken() + "&allow_source=true&player=twitchweb" +
                "&allow_spectre=true&allow_audio_only=true";
        File downloadedFile = null;
        try {
            URL downloadUrl = new URL(url);
            downloadedFile = File.createTempFile("TwitchForge-Playlist", ".m3u8");
            downloadedFile.deleteOnExit();

            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();

            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(downloadedFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parseFeeds(FileUtils.read(downloadedFile.getAbsolutePath()));
    }

    private Token retrieveToken(String vodId) {
        Variables variables = Variables.builder()
                .isLive(false)
                .login("")
                .isVod(true)
                .vodID(vodId)
                .playerType("channel_home_live")
                .build();

        PlaybackAccessTokenRequest request = PlaybackAccessTokenRequest.builder()
                .operationName("PlaybackAccessToken")
                .variables(variables)
                .extensions(extensions)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-ID", CLIENT_ID);
        HttpEntity<PlaybackAccessTokenRequest> requestEntity = new HttpEntity<>(request, headers);
        PlaybackAccessTokenResponse response = restTemplate
                .exchange(TOKEN_URL, HttpMethod.POST, requestEntity, PlaybackAccessTokenResponse.class)
                .getBody();

        if (response.getData().getVideoPlaybackAccessToken() == null){
            throw new BadResponseException();
        }

        return Token.builder()
                .signature(response.getData().getVideoPlaybackAccessToken().getSignature())
                .token(response.getData().getVideoPlaybackAccessToken().getValue())
                .build();
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
