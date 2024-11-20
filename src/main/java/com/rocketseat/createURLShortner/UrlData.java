package com.rocketseat.createURLShortner;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class UrlData {
    private String originalUrl;
    private Long expirationTime;


}
