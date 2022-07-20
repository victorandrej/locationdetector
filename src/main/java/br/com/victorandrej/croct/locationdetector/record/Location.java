package br.com.victorandrej.croct.locationdetector.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Location(@JsonProperty(required = true) float latitude, @JsonProperty(required = true) float longitude,
		@JsonProperty(required = true) String country_name, @JsonProperty(required = true) String region_name,
		@JsonProperty(required = true) String city) {
}
