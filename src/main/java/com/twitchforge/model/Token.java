package com.twitchforge.model;

import lombok.*;
import lombok.Data;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    private String signature;
    private String token;
}
