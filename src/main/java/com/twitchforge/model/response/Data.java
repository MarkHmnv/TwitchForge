package com.twitchforge.model.response;

import com.twitchforge.model.request.VideoPlaybackAccessTokenValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Data {
    private VideoPlaybackAccessTokenValue videoPlaybackAccessToken;
}
