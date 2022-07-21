package br.com.victorandrej.locationdetector;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.victorandrej.croct.locationdetector.LocationDetector;
import br.com.victorandrej.croct.locationdetector.enums.ResponseStatus;
import br.com.victorandrej.croct.locationdetector.record.Request;
import br.com.victorandrej.croct.locationdetector.record.TopicResponse;
import br.com.victorandrej.croct.locationdetector.serializer.JacksonDeserializer;
import br.com.victorandrej.croct.locationdetector.serializer.JacksonSerializer;
import br.com.victorandrej.croct.locationdetector.service.kafka.KafConsumer;
import br.com.victorandrej.croct.locationdetector.service.kafka.enums.ConsumerStatus;
import br.com.victorandrej.croct.locationdetector.util.PropertiesUtils;

class LocationDetectorTest {
	// Sem esta propriedade um teste interfira no outro

	KafConsumer<String, Request> serverConsumer;
	KafkaProducer<String, Request> producer;
	KafkaConsumer<String, TopicResponse> consumer;

	void iniciarLocationDetector() throws InterruptedException {
		LocationDetector locationDetector = new LocationDetector(ApiToken.TOKEN, "localhost:9092",
				LocationDetector.LOCATION_DETECTOR_RESPONSE, "false", 1000, 120);

		serverConsumer = new KafConsumer<>(
				PropertiesUtils.createConsumerProperties("localhost:9092", LocationDetector.LOCATION_GROUP),
				Arrays.asList(LocationDetector.LOCATION_DETECTOR_REQUEST), locationDetector);
		new Thread(serverConsumer).start();

		while (!serverConsumer.getStatus().equals(ConsumerStatus.RUNNING)) {
			Thread.sleep(100);
		}
	}

	void iniciarProducerEConsumer() {
		Properties prop = new Properties();
		prop.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		prop.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		prop.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonSerializer.class.getName());
		producer = new KafkaProducer<>(prop);

		Properties consumerPropeties = new Properties();
		consumerPropeties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		consumerPropeties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		consumerPropeties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				JacksonDeserializer.class.getName());
		consumerPropeties.setProperty(JacksonDeserializer.JACKSONTYPE, TopicResponse.class.getName());
		consumerPropeties.setProperty(ConsumerConfig.GROUP_ID_CONFIG,
				"CONSUMER_TEST_GROUP" + Integer.toString(new Random().nextInt()));
		consumer = new KafkaConsumer<>(consumerPropeties);
		consumer.subscribe(Arrays.asList(LocationDetector.LOCATION_DETECTOR_RESPONSE));
	}

	@BeforeEach
	void init() throws InterruptedException {
		iniciarLocationDetector();
		iniciarProducerEConsumer();
	}

	@AfterEach
	void pararLocationDetector() throws InterruptedException {
		serverConsumer.stop();
		while (!serverConsumer.getStatus().equals(ConsumerStatus.DEAD)) {
			Thread.sleep(100);
		}
	}

	private Optional<TopicResponse> waitForResponse(long seconds) {
		LocalTime time = LocalTime.now();
		while (true) {
			for (var item : consumer.poll(Duration.ofMillis(100)))
				return Optional.ofNullable(item.value());
			if (time.plusSeconds(seconds).isBefore(LocalTime.now()))
				return Optional.ofNullable(null);
		}

	}

	@Test
	void enviarSolicitacaoCorretaParaOServidor() throws InterruptedException, ExecutionException {
		var request = new Request("victorandrej", Instant.now().toEpochMilli(), "www.google.com");
		producer.send(new ProducerRecord<String, Request>(LocationDetector.LOCATION_DETECTOR_REQUEST, request)).get();
		var response = waitForResponse(60);
		if(response.isPresent())
			Assert.assertEquals(response.get().status(), ResponseStatus.OK);
		else
			Assert.fail();
	}

	@Test
	void enviarSolicitacaoErradaParaOSerivdor() throws Exception {
		var request = new Request("victorandrej", Instant.now().toEpochMilli(), "http://www.google.com");
		producer.send(new ProducerRecord<String, Request>(LocationDetector.LOCATION_DETECTOR_REQUEST, request)).get();
		var response = waitForResponse(60);
		if(response.isPresent())
			Assert.assertNotEquals(response.get().status(), ResponseStatus.OK);
		else
			Assert.fail();
	}

}
