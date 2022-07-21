package br.com.victorandrej.croct.locationdetector.util;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import br.com.victorandrej.croct.locationdetector.record.Request;
import br.com.victorandrej.croct.locationdetector.serializer.JacksonDeserializer;
import br.com.victorandrej.croct.locationdetector.serializer.JacksonSerializer;

public final class PropertiesUtils {
	private PropertiesUtils() {}

	public static Properties createConsumerProperties(String server, String groupId) {
		Properties propeties = new Properties();
		propeties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
		propeties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		propeties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonDeserializer.class.getName());
		propeties.setProperty(JacksonDeserializer.JACKSONTYPE, Request.class.getName());
		propeties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		return propeties;
	}

	public static Properties createProducerProperties(String server) {
		Properties propeties = new Properties();
		propeties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
		propeties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		propeties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonSerializer.class.getName());
		return propeties;
	}
	
	
}
