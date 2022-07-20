package br.com.victorandrej.croct.locationdetector.serializer;

import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonSerializer<T> implements Serializer<T> {

	@Override
	public byte[] serialize(String topic, T data) {
		try {
			return new ObjectMapper().writeValueAsString(data).getBytes();
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
