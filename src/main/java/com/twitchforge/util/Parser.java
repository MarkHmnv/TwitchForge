package com.twitchforge.util;

import com.twitchforge.model.Feeds;
import com.twitchforge.model.enums.Quality;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    public static Feeds parseFeeds(List<String> response) {
        Feeds feeds = new Feeds();
        for (int i = 0; i < response.size(); i++) {
            String currentLine = response.get(i);
            String previousLine = getLine(response, i-1);
            String previousPreviousLine = getLine(response, i - 2);

            if (!currentLine.startsWith("#")) {
                if (previousPreviousLine.contains("chunked")) {
                    parseSourceFeed(currentLine, previousLine, previousPreviousLine, feeds);
                } else if (previousPreviousLine.contains("audio")) {
                    feeds.getFeedsMap().put(currentLine, Quality.AUDIO);
                } else if (previousPreviousLine.contains("1080p60")) {
                    parseResolutionFeed(currentLine, previousLine, previousPreviousLine, feeds);
                } else {
                    parseDefaultFeed(currentLine, previousPreviousLine, feeds);
                }
            }
        }
        return feeds;
    }

    private static String getLine(List<String> response, int index) {
        return (index > 0) ? response.get(index) : "";
    }

    private static void parseSourceFeed(String currentLine, String previousLine, String previousPreviousLine, Feeds feeds) {
        feeds.getFeedsMap().put(currentLine, Quality.SOURCE);
        double fps = 60;
        if (previousPreviousLine.contains("Source")) {
            String resolution = singleRegex("#EXT-X-STREAM-INF:BANDWIDTH=\\d*,CODECS=\"[a-zA-Z0-9.]*,[a-zA-Z0-9.]*\",RESOLUTION=(\\d*x\\d*),VIDEO=\"chunked\"", previousLine);
            feeds.getFeedsMap().put(currentLine, Quality.getQualityByResolutionAndFps(resolution, fps));
        } else {
            String patternF = "#EXT-X-MEDIA:TYPE=VIDEO,GROUP-ID=\"chunked\",NAME=\"([0-9p]*) \\(source\\)\",AUTOSELECT=[\"YES\"||\"NO\"]*,DEFAULT=[\"YES\"||\"NO\"]*";
            Pattern pF = Pattern.compile(patternF);
            Matcher mF = pF.matcher(previousPreviousLine);
            fps = 0;
            if (mF.find()) {
                String vid = mF.group(1);
                fps = Double.parseDouble(vid.substring(vid.indexOf('p') + 1));
            }
            String pattern = "#EXT-X-STREAM-INF:BANDWIDTH=\\d*,RESOLUTION=(\\d*x\\d*),CODECS=\"[a-zA-Z0-9.]*,[a-zA-Z0-9.]*\",VIDEO=\"chunked\"";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(previousLine);
            if (m.find()) {
                feeds.getFeedsMap().put(currentLine, Quality.getQualityByResolutionAndFps(m.group(1), fps));
            }
        }
    }

    private static void parseResolutionFeed(String currentLine, String previousLine, String previousPreviousLine, Feeds feeds) {
        String patternF = "#EXT-X-MEDIA:TYPE=VIDEO,GROUP-ID=\"1080p[0-9]*\",NAME=\"(1080p[0-9]*)\",AUTOSELECT=[\"YES\"||\"NO\"]*,DEFAULT=[\"YES\"||\"NO\"]*";
        Pattern pF = Pattern.compile(patternF);
        Matcher mF = pF.matcher(previousPreviousLine);
        double fps = 0;
        if (mF.find()) {
            String vid = mF.group(1);
            fps = Double.parseDouble(vid.substring(vid.indexOf('p') + 1));
        }
        String pattern = "#EXT-X-STREAM-INF:BANDWIDTH=\\d*,CODECS=\"[a-zA-Z0-9.]*,[a-zA-Z0-9.]*\",RESOLUTION=(\\d*x\\d*),VIDEO=\"1080p[0-9]*\"";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(previousLine);
        if (m.find()) {
            feeds.getFeedsMap().put(currentLine, Quality.getQualityByResolutionAndFps(m.group(1), fps));
        }
    }

    private static void parseDefaultFeed(String currentLine, String previousPreviousLine, Feeds feeds) {
        String video = singleRegex("#EXT-X-MEDIA:TYPE=VIDEO,GROUP-ID=\"([\\d]*p[36]0)\",NAME=\"([0-9p]*)\",AUTOSELECT=[\"YES\"||\"NO\"]*,DEFAULT=[\"YES\"||\"NO\"]*", previousPreviousLine);
        Quality quality = Quality.getQualityByVideo(video);
        feeds.getFeedsMap().put(currentLine, quality);
    }

    private static String singleRegex(String pattern, String value) {
        Matcher matcher = Pattern.compile(pattern).matcher(value);
        return matcher.find() ? matcher.group(1) : null;
    }
}
