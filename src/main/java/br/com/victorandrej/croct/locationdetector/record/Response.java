package br.com.victorandrej.croct.locationdetector.record;

public record Response(String userId, long timeStamp, String ip, Location location) {
}
