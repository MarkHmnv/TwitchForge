package com.twitchforge.model.request;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.twitchforge.model.Extensions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PlaybackAccessTokenRequest {
    private String operationName;
    private Variables variables;
    private Extensions extensions;
}
