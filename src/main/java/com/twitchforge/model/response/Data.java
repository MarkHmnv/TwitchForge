package com.twitchforge.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@lombok.Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Data {
    private String id;
    private String streamId;
    private String userId;
    private String userLogin;
    private String userName;
    private String title;
    private String description;
    private String createdAt;
    private String publishedAt;
    private String url;
    private String thumbnailUrl;
    private String viewable;
    private int viewCount;
    private String language;
    private String type;
    private String duration;
    private Object mutedSegments;
}
