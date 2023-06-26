package com.twitchforge.model.response;

import com.twitchforge.model.Extensions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaybackAccessTokenResponse {
    private com.twitchforge.model.response.Data data;
    private Extensions extensions;
}
