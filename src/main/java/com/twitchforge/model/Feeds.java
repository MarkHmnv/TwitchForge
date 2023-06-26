package com.twitchforge.model;

import com.twitchforge.model.enums.Quality;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Feeds {
    private Map<String, Quality> feedsMap = new LinkedHashMap<>();
}
