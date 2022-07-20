package br.com.victorandrej.croct.locationdetector.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Response(String userId, long timeStamp, String ip, Location location) {
}
