package com.twitchforge.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@lombok.Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TwitchTrackerResponse {
    private String streamerUsername;
    private long streamId;
    private String startTime;
}
