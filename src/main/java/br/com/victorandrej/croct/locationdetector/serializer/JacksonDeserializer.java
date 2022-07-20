package br.com.victorandrej.croct.locationdetector.serializer;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonDeserializer<T> implements Deserializer<T> {

	public static final String JACKSONTYPE = "JACKSONDESERIALIZER";

	private Class<T> classType;

	@SuppressWarnings("unchecked")
	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		try {
			this.classType = (Class<T>) Class.forName((String) configs.get(JacksonDeserializer.JACKSONTYPE));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Propriedade " + JacksonDeserializer.JACKSONTYPE + " nao atribuida");
		}
	}

	@Override
	public T deserialize(String topic, byte[] data) {
		try {
			return new ObjectMapper().readValue(new String(data), classType);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
