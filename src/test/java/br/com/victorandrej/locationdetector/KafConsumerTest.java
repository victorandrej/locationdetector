package br.com.victorandrej.locationdetector;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.naming.TimeLimitExceededException;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.victorandrej.croct.locationdetector.service.kafka.KafConsumer;
import br.com.victorandrej.croct.locationdetector.service.kafka.enums.ConsumerStatus;
import br.com.victorandrej.croct.locationdetector.service.kafka.exception.KafConsumerDeadException;

/**
 * 
 * @author victor
 *
 */
class KafConsumerTest {
	KafkaProducer<String, String> producer;
	Properties consumerPropeties;

	@BeforeEach
	void inicio() {

		Properties prop = new Properties();
		prop.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		prop.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		prop.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		producer = new KafkaProducer<>(prop);

		consumerPropeties = new Properties();
		consumerPropeties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		consumerPropeties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		consumerPropeties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				StringDeserializer.class.getName());
		consumerPropeties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, Integer.toString(new Random().nextInt()));
	}

	@Test
	void forcarParadaTest() throws InterruptedException, ExecutionException, TimeLimitExceededException {
		String topic = Integer.toString(new Random().nextInt());
		for (int i = 0; i < 20; i++)
			producer.send(new ProducerRecord<String, String>(topic, "Chave", "Valor")).get();

		LocalTime time = LocalTime.now();
		KafConsumer<String, String> kafConsumer = new KafConsumer<>(consumerPropeties, Arrays.asList(topic), (r) -> {
		});
		Thread t = new Thread(kafConsumer);
		t.start();
		
		while (true) {
			kafConsumer.stop();

			if (!kafConsumer.getStatus().equals(ConsumerStatus.RUNNING))
				return;

			if (time.plusSeconds(30).isBefore(LocalTime.now()))
				throw new TimeLimitExceededException("Thread nao parou no tempo determinado");
		}

	}

	@Test
	void naoIniciarAposParadoTest() {
		Assert.assertThrows(KafConsumerDeadException.class, () -> {
			try (var kafConsumer = new KafConsumer<>(consumerPropeties, Arrays.asList("REQUEST"), (r) -> {
			})) {
				kafConsumer.stop();
				kafConsumer.run();
			}
		});
	}

}
